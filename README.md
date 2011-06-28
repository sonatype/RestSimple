RestSimple Quick Start
======================

RestSimple is a framework for dramatically enhancing the building of REST based application. It's simple. You develop your REST application using RestSimple API and the framework generate and deploy your resource for you. Suddenly, your REST Application is boasting some pretty impressive features like:

* Automatic generation of the client side
* Version/Content Negotiation support
* Use DSL to write REST application
* Extremely simple to deploy

The framework goal is to easy allow programmatic, embeddable and portable REST applications. The framework is composed by the following module

* restsimple-api: the core classes of the framework. It contains API for application and SPI for framework who want to support RestSimple
* restsimple-client: a client library for RestSimple. It can generate proxy from an annotated interface, or you can directly use a client API.
* restsimple-jaxrs: an implementation of the restsimple-api using Jersey.
* restsimple-sitebricks: an implementation of the restsimple-api using Sitebricks
* restsimple-templating: generate HTML file from ServiceDefinition (similar to Enunciate)
* restsimple-service-descriptor-creator: generate ServiceDefinition from POJO object following some convention
* restsimple-webdriver: a driver for testing RestSimple application
* restsimple-acceptance-test: test/application for RestSimple

RestSimple API
==============

Browse it: http://sonatype.github.com/RestSimple/

Building a RestSimple Application.
==================================

A RestSimple application consist of a ServiceDefinition, ServiceHandler and Action. The main component of a RestSimple application is called a ServiceDefinition. A ServiceDefinition contains all information about path, serialization and deserialization of objects, entity to invoke (action), etc. To demonstrate how it works, let's build a really simple pet store application.

    Action action = new PetstoreAction();
    DefaultServiceDefinition serviceDefinition = new DefaultServiceDefinition();
    serviceDefinition
       .withHandler(new GetServiceHandler("getPet", action).consumeWith(JSON, Pet.class).producing(JSON))
       .withHandler(new PostServiceHandler("addPet", action).consumeWith(JSON, Pet.class).producing(JSON));

First, you need to define an action. An action is where the business logic reside. The Action interface is simply defined as:

    public interface Action<T, U> {

       /**
        * Execute an action. Actions is HTTP Method Verb executed on HTTP Path Noun.
        * @param actionContext an {@link ActionContext}
        * @return T a response to be serialized
        */
       public T action(ActionContext<U> actionContext) throws ActionException;

Second, let's define a very simple Action. Let's just persist our Pet in memory, and make sure a REST requests can retrieve those pets. Something as simple as:

    public class PetstoreAction extends TypedAction<Pet, Pet> {

        public final static String APPLICATION = "application";
        public final static String JSON = "json";
        public final static String TEXT = "txt";
        public final static String PET_EXTRA_NAME = "petType";
        private final ConcurrentHashMap<String, Pet> pets = new ConcurrentHashMap<String, Pet>();

        @Override
        public Pet get(ActionContext<Pet> actionContext) {
            String pathValue = actionContext.pathParams().get("pet");
            Map<String, Collection<String>> headers = actionContext.headers();

            Pet pet = pets.get(pathValue);
            if (pet != null) {

                if (headers.size() > 0) {
                    for (Map.Entry<String, Collection<String>> e : headers.entrySet()) {
                        if (e.getKey().equals("Cookie")) {
                            pet.setName(pet.getName() + "--" + e.getValue().iterator().next());
                            break;
                        }
                    }
                }
            }
            return pet;
        }

        @Override
        public Pet post(ActionContext<Pet> actionContext) {
            String pathValue = actionContext.pathParams().get("pet");

            Pet pet = actionContext.get();
            String value = pathValue;
            pets.put(value, pet);
            return pet;
        }

        @Override
        public Pet delete(ActionContext<Pet> actionContext) {
            String pathValue = actionContext.pathParams().get("pet");

            return pets.remove(pathValue);
        }
    }

Note the type of our PetAction: <Pet,Pet>: that simply means the Action will consume Pet instance, and also produce Pet. The ActionContext.get() operation will return a Pet object. This object is automatically de-serialized by the framework using the information contained in the ServiceDefinition (more on that later). The Pet object can simply be defined as:

    public class Pet {

      private String name;

      public Pet() {
      }

      public Pet(String name) {
        this.name = name;
      }

      public String getName() {
        return name;
      }

      public void setName(String name) {
        this.name = name;
      }

      @Override
      public String toString() {
        return "Pet{" +
                "name='" + name + '\'' +
                '}';
      }
    }

The serialization and deserialization of the Pet class will be handled be the RestSimple framework itself. Next step is to map our Action to some URL. With RestSimple, this is done using ServiceHandler. A ServiceHandler is a simple placeholder for defining how an Action are mapped from a URL. Simply said, you define a ServiceHandler as:

    new GetServiceHandler("/getPet/{pet}", action).consumeWith(JSON, Pet.class).producing(JSON);

and then add it to a ServiceDefinition like

    serviceDefinition.withHandler(new GetServiceHandler ... )

You can also let the ServiceDefinition creates the ServiceHandler for you as:

    serviceDefinition.producing(new MediaType("application","json");
    serviceDefinition.get("/getPet/{pet}", action); // same as new GetServiceHandler(...)

The code snippet above map an Action to an HTTP Get operation, consuming JSON and producing JSON. If you are familiar with JAX-RS, the functionality would be defined as

    @Get
    @Produces("application/json")
    @Consumes("application/json")
    public Response invokeAction(@PathParam("getPet") Pet pet) {...}

An HTTP POST operation can simply be defined as:

    new PostServiceHandler("addPet", action).addFormParam("petColor").addFormParam("petAge");

Now before deploying our ServiceDefinition, let's define it completely:

    Action action = new PetstoreAction();
    serviceDefinition = new DefaultServiceDefinition();
    serviceDefinition
           .withPath("/")
           .withHandler(new GetServiceHandler("/getPet/{pet}", action).consumeWith(JSON, Pet.class).producing(JSON))
           .withHandler(new DeleteServiceHandler("/deletePet/{pet}", action).consumeWith(JSON, Pet.class).producing(JSON))
           .withHandler(new PostServiceHandler("/addPet/{pet}", action).consumeWith(JSON, Pet.class).producing(JSON));

That's it.

You can also generate ServiceDefinition on the fly from any POJO object. Let's say we have a POJO defined as:

    public class AddressBook {
        private final Map<String, Person> peoplea = new LinkedHashMap<String, Person>();;

        private static int idx = 4;

        public AddressBook() {
        }

        public Person createPerson(Person person) {
            peoplea.put(person.id, person);
            return person;
        }

        public Person readPerson(String id) {
            return peoplea.get(id);
        }

        public Collection<Person> readPeople() {
            return peoplea.values();
        }

        public Person updatePerson(Person person) {
            System.out.println(person);
            peoplea.put(person.id, person);

            return person;
        }

        public Person deletePerson(String id) {
            return peoplea.remove(id);
        }
    }

You can generate a ServiceDefinition by doing:

    MethodServiceDefinitionBuilder serviceDefinitionBuilder = new MethodServiceDefinitionBuilder();
    ServiceDefinition serviceDefinition = serviceDefinitionBuilder.type(AddressBook.class).build();

The ServiceDefinition will be generated using the following template:

    GET         readXXX
    POST        createXXX
    PUT         updateXXXX
    DELETE      delete

You can customize the method's name to HTTP method operation

    import static org.sonatype.restsimple.creator.ServiceDefinitionCreatorConfig.METHOD;
    import static org.sonatype.restsimple.creator.ServiceDefinitionCreatorConfig.config;
    ....

    MethodServiceDefinitionBuilder serviceDefinitionBuilder = new MethodServiceDefinitionBuilder();
    ServiceDefinition serviceDefinition =
         serviceDefinitionBuilder.type(AddressFooBook.class).config(config().map("foo", METHOD.POST)
                                                                            .map("bar", METHOD.GET)
                                                                            .map("pong", METHOD.DELETE)).build();

Hence a method starting with foo will be mapped to a POST operation etc.

Extending RestSimple via native REST support
============================================
Currently RestSimple supports Sitebricks and JAX-RS (Jersey). It is possible to extend a ServiceDefinition with Sitebricks or Jaxrs annotation invoking the extendedWith:


    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new RestSimpleSitebricksModule() {

            @Override
            public List<ServiceDefinition> defineServices(Injector injector) {
                Action action = new PetstoreAction();
                List<ServiceDefinition> list = new ArrayList<ServiceDefinition>();

                ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
                serviceDefinition
                        .withHandler(new GetServiceHandler("/get/:pet", action).consumeWith(JSON, Pet.class).producing(JSON))
                        .withHandler(new PostServiceHandler("/create/:pet", action).consumeWith(JSON, Pet.class).producing(JSON))
                        .extendWith(Extension.class);

                list.add(serviceDefinition);
                return list;
            }
        });
    }

    @At("/lolipet/:myPet")
    public final static class Extension {
        @Get
        @Accept("application/vnd.org.sonatype.rest+json")
        public Reply<?> lolipet(){
            Pet pet = new Pet("lolipet");
            return Reply.with(pet).as(Json.class);
        }
    }

Using RestSimple with existing JAXRS or Sitebricks Resource
============================================================
Existing JAXRS or Sitebricks resource can be used as it with RestSimple. All you need to do is to tell RestSimple the package name of your resource. As simple as:

    public class NativeJaxrsConfig extends GuiceServletContextListener {

        @Override
        protected Injector getInjector() {
            RestSimpleJaxrsModule module = new RestSimpleJaxrsModule();
            config.scan( Extension.class.getPackage() );
            return Guice.createInjector(module);
        }

        @Path("/lolipet/{myPet}")
        public final static class Extension {
            @GET
            @Consumes("application/vnd.org.sonatype.rest+json")
            public Pet lolipet(){
                return new Pet("lolipet");
            }
        }
    }


Deploying your RestSimple application
=====================================

There is many ways to deploy a ServiceDefinition. First, you need to decide which framework you want to deploy to. Currently, RestSimple supports JAX-RS and Sitebricks. Deploying a ServiceDefinition is as simple as:

    public class PetstoreJaxrsConfig extends GuiceServletContextListener {

        @Override
        protected Injector getInjector() {
            return Guice.createInjector(new RestSimpleJaxrsModule() {

                private final MediaType JSON = new MediaType(PetstoreAction.APPLICATION, PetstoreAction.JSON);

                @Override
                public List<ServiceDefinition> defineServices(Injector injector) {

                    Action action = new PetstoreAction();
                    // Can also be created using new DefaultServiceDefinition();
                    ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class)
                    serviceDefinition
                       .consuming(JSON)
                       .producing(JSON);
                       .get("/getPet/{pet}", action)
                       .delete("/deletePet/{pet}", action)
                       .post("/addPet/{pet}", action);

                    list.add(serviceDefinition);
                    return list;
                }
            });
        }
    }

All you need to do is to extends the appropriate Guice Module: RestSimpleJaxrsModule or RestSimpleSitebricksModule. The abstract method defineServices is where you define one or many ServiceDefinition. That's it: the framework will take care of deploying your ServiceDefinition.

You can also let scan for classes annotated with the @Service annotation that contains a method returning a ServiceDefinition like:

    @Service
    public class Foo {

       @Inject
       ServiceDefinition serviceDefinition;

       public ServiceDefinition create() {
           Action action = new PetstoreAction();

           serviceDefinition
               .consuming(JSON)
               .producing(JSON);
               .get("/getPet/{pet}", action)
               .delete("/deletePet/{pet}", action)
               .post("/addPet/{pet}", action);

           return serviceDefinition
       }
    }

and then just do:

     public class ScanServiceDefinition extends GuiceServletContextListener {

        @Override
        protected Injector getInjector() {
            RestSimpleJaxrsModule module = new RestSimpleJaxrsModule();
            config.scan( Foo.class.getPackage() );
            return Guice.createInjector(module);
        }
    }

You can also add ServiceDefinition classes or instance directly by doing:

     public class ManuallyServiceDefinition extends GuiceServletContextListener {

        @Override
        protected Injector getInjector() {
            RestSimpleJaxrsModule module = new RestSimpleJaxrsModule();

            module.addClass( Foo.class );  // Guice will take care of creating the class.
            module.addInstance ( new Foo() );

            return Guice.createInjector(module);
        }
    }


Building the client side of a RestSimple application.
=====================================================

There is several ways to define your RestSimple application:
* By using the AHC library
* By using the RestSimple's Web class
* By generating a proxy using RestSimple annotation

Using AHC library

Once a RestSimple application is deployed, you can simply use the AsyncHttpClient library (AHC) by doing:

    AsyncHttpClient c = new AsyncHttpClient();
    Response r = c.preparePost(targetUrl + "/addPet/myPet")
                  .setBody("{\"name\":\"pouetpouet\"}")
                  .addHeader("Content-Type", "application/json")
                  .addHeader("Accept", "application/json").execute()
                  .get();
    Pet pet = new Pet(response.getResponseBodyAsString());

Using the Web class

You can also use the RestSimple's Web class to invoke your ServiceDefinition

    WebClient webClient = new WebClient(serviceDefinition);
    Pet pet = webClient.clientOf(targetUrl + "/addPet/myPet")
                       .post(new Pet("pouetpouet"), Pet.class);

The webClient class is constructed and can derive some information from a ServiceDefinition instance, which includes the content-type and accept headers.

Using the WebProxy class

Finally, you can also generate a client implementation using the WebProxy class. First you need to define an interface and annotate methods using the RestSimple annotation set.

    public static interface PetClient {

        @Get
        @Path("getPet")
        @Produces("application/json)
        public Pet get(@PathParam("myPet") String path);

        @Post
        @Path("addPet")
        @Produces("application/json)
        public Pet post(@PathParam("myPet") String myPet, String body);

        @Delete
        @Path("deletePet")
        @Produces("application/json)
        public Pet delete(@PathParam("myPet") String path);

    }

Then you can generate the client the implementation by simply doing:

    PetClient client = WebProxy.createProxy(PetClient.class, URI.create(targetUrl));
    Pet pet = client.post(new Pet("pouetpouet"), "myPet");

Generating ServiceDefinition HTML description
=============================================

  It is possible to generate an HTML view of a ServiceDefinition. All you need to do is

        ServiceDefinition serviceDefinition = new DefaultServiceDefinition();
        serviceDefinition
                .withHandler(new PutServiceHandler("/createAddressBook/:ad", action))
                .withHandler(new GetServiceHandler("/getAddressBook/:ad", action))
                .withHandler(postServiceHandler)
                .withHandler(new DeleteServiceHandler("/deleteAddressBook/:ad", action));

        HtmlTemplateGenerator generator = new HtmlTemplateGenerator(new VelocityTemplater());

The Html page will looks like:

        <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
                "http://www.w3.org/TR/html4/loose.dtd">
        <html>
        <head>
            <title>ServiceDefinitionTemplate</title>
        </head>
        <body>
        <h1>ServiceDefinition</h1>

        <div name="ServiceDefinition">
            <p class="rooturi">
                ROOT URI: /serviceDefinition
            </p>

            <p class="consuming">
                Consuming:
            <ul>
                            <li>application/vnd.org.sonatype.rest+json</li>
                    </ul>
            </p>
            <p class="producing">
                Producing:
            <ul class="ul-producing">
                            <li class="li-producing">application/vnd.org.sonatype.rest+json</li>
                            <li class="li-producing">application/vnd.org.sonatype.rest+xml</li>
                    </ul>
            </p>

        </div>
                <h2> ServiceHandler </h2>

            <div class="ServiceHandler">
                <p class="uri">
                    URI: /serviceDefinition/createAddressBook/:ad
                </p>

                <p class="method">
                    Method: PUT
                </p>

                            <p>
                    Consuming: application/vnd.org.sonatype.rest+json
                    </p>

                <p class="sh-producing">
                    Producing:
                <ul>
                            </ul>
                </p>
                <p class="action">
                    Action: AddressBookAction
                </p>
            </div>
                <h2> ServiceHandler </h2>

            <div class="ServiceHandler">
                <p class="uri">
                    URI: /serviceDefinition/getAddressBook/:ad
                </p>

                <p class="method">
                    Method: GET
                </p>

                            <p>
                    Consuming: application/vnd.org.sonatype.rest+json
                    </p>

                <p class="sh-producing">
                    Producing:
                <ul>
                            </ul>
                </p>
                <p class="action">
                    Action: AddressBookAction
                </p>
            </div>
                <h2> ServiceHandler </h2>

            <div class="ServiceHandler">
                <p class="uri">
                    URI: /serviceDefinition/updateAddressBook/:ad
                </p>

                <p class="method">
                    Method: POST
                </p>

                            <p>
                    Consuming: application/vnd.org.sonatype.rest+json
                    </p>

                <p class="sh-producing">
                    Producing:
                <ul>
                            </ul>
                </p>
                <p class="action">
                    Action: AddressBookAction
                </p>
            </div>
                <h2> ServiceHandler </h2>

            <div class="ServiceHandler">
                <p class="uri">
                    URI: /serviceDefinition/deleteAddressBook/:ad
                </p>

                <p class="method">
                    Method: DELETE
                </p>

                            <p>
                    Consuming: application/vnd.org.sonatype.rest+json
                    </p>

                <p class="sh-producing">
                    Producing:
                <ul>
                            </ul>
                </p>
                <p class="action">
                    Action: AddressBookAction
                </p>
            </div>
            </body>
        </html>

Content Negotiation
===================
RestSimple supports content negotiation. By default, RestSimple support RFC 2295

      http://www.ietf.org/rfc/rfc2295.txt

Both client and server supports that RFC, and you can enable it by doing:

        WebClient webClient = new WebAHCClient(serviceDefinition);
        Map<String, String> m = new HashMap<String, String>();
        m.put("Content-Type", acceptHeader);
        m.put("Accept", "application/xml");

        Pet pet = webClient.clientOf(targetUrl + "/addPet/myPet")
                .headers(m)
                .supportedContentType(new MediaType("application","json"))
                .post(new Pet("pouetpouet"), Pet.class);

        pet = webClient.clientOf(targetUrl + "/getPet/myPet")
                .headers(m)
                .get(Pet.class);

 In the snippet above the Accept header is first set to application/xml. If the server doesn't support that media type, line

        .supportedContentType(new MediaType("application","json"))

 will tell the client to re-try using application/json. 