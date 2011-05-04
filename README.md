RestSimple Quick Start

RestSimple is a framework for dramatically enhancing the building of REST based application. It's simple. You develop your REST application using RestSimple API and the framework generate and deploy your resource for you. Suddenly, your REST Application is boasting some pretty impressive features like:

* Automatic generation of the client side
* Version/Content Negotiation support
* Use DLS to write REST application
* Extremely simple to deploy

The framework goal is to easy allow programmatic, embeddable and portable REST applications. The framework is composed by the following module

* restsimple-api: the core classes of the framework.
* restsimple-client: a client library for RestSimple.
* restsimple-jaxrs: an implementation of the restsimple-api using Jersey.
* restsimple-sitebricks: an implementation of the restsimple-api using Sitebricks

Building a RestSimple Application.

A RestSimple application consist of ServiceDefinition, ServiceHandler and Action. The main component of a RestSimple application is called a ServiceDefinition. A ServiceDefinition contains all information about path, serialization and deserialization of objects, entity to invoke (action), etc. To demonstrate how it works, let's build a really simple pet store application.

    Action action = new PetstoreAction();
    DefaultServiceDefinition serviceDefinition = new DefaultServiceDefinition();
    serviceDefinition
       .withHandler(new GetServiceHandler("getPet", action).consumeWith(JSON, Pet.class).producing(JSON))
       .withHandler(new PostServiceHandler("addPet", action).consumeWith(JSON, Pet.class).producing(JSON));

First, you need to define an action. An action is where the business logic reside. The Action interface is simply defined as:


public interface Action<T, U> {

   public T action(ActionContext<U> actionContext) throws ActionException;

Second, let's define a very simple Action. Let's just persist our Pet in memory, and make sure a REST request can retrieve those pets. Something as simple as:

`
public class PetstoreAction implements Action<Pet, Pet> {

    private final ConcurrentHashMap<String, Pet> pets = new ConcurrentHashMap<String, Pet>();

    @Override
    public Pet action(ActionContext<Pet> actionContext) throws ActionException {

        switch (actionContext.method()) {
            case GET:
                Pet pet = pets.get(actionContext.pathValue());

                return pet;
            case DELETE:
                return pets.remove(actionContext.pathValue());
            case POST:
                pet = actionContext.get();

                pets.put(actionContext.pathValue(), pet);
                return pet;
            default:
                throw new ActionException(405);
        }
    }
`
Note the type of our PetAction: <Pet,Pet>: that simply means the Action will consume Pet instance, and also produce Pet. The ActionContext.get() operation will return a Pet object. This object is automatically de-serialized by the framework by using the information contained in the ServiceDefinition (more on that later). The Pet object can simply be defined as:

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

   new GetServiceHandler("getPet", action).consumeWith(JSON, Pet.class).producing(JSON);

The line above map an Action to an HTTP Get operation, consuming JSON and producing JSON. If you are familiar with JAXRS, the functionality would be defined as

    @Get
    @Produces
    @Consumes
    public Response invokeAction(@PathParam("getPet") Pet pet) {...}

An HTTP POST operation can simply be defined as:

    new PostServiceHandler("addPet", action).addFormParam("petColor").addFormParam("petAge");

Now before deploying our ServiceDefinition, let's define it completely:

    Action action = new PetstoreAction();
    serviceDefinition = new DefaultServiceDefinition();
    serviceDefinition
           .withPath("/")
           .withHandler(new GetServiceHandler("getPet", action).consumeWith(JSON, Pet.class).producing(JSON))
           .withHandler(new DeleteServiceHandler("deletePet", action).consumeWith(JSON, Pet.class).producing(JSON))
           .withHandler(new PostServiceHandler("addPet", action).consumeWith(JSON, Pet.class).producing(JSON));

That's it.

Deploying your RestSimple application

There is many ways to deploy a ServiceDefinition. First, you need to decide which framework you want to deploy to. Currently, RestSimple supports JAXRS and Sitebricks. Deploying a ServiceDefinition is as simple as:

    public class PetstoreJaxrsConfig extends GuiceServletContextListener {

        @Override
        protected Injector getInjector() {
            return Guice.createInjector(new JaxrsConfig() {

                private final MediaType JSON = new MediaType(PetstoreAction.APPLICATION, PetstoreAction.JSON);

                @Override
                public List<ServiceDefinition> defineServices(Injector injector) {

                    Action action = new PetstoreAction();
                    ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class) // Can also be created using new DefaultServiceDefinition();
                    serviceDefinition
                       .withHandler(new GetServiceHandler("getPet", action).consumeWith(JSON, Pet.class).producing(JSON))
                       .withHandler(new DeleteServiceHandler("deletePet", action).consumeWith(JSON, Pet.class).producing(JSON))
                       .withHandler(new PostServiceHandler("addPet", action).consumeWith(JSON, Pet.class).producing(JSON));

                    list.add(serviceDefinition);
                    return list;
                }
            });
        }
    }

All you need to do is to extends the appropriate ServiceDefinitionConfig: JaxrsConfig or SitebricksConfig. The abstract method _defineServices_ is where you define one or many ServiceDefinition. That's it: the framework will take care of deploying your ServiceDefinition.

Building the client side of a RestSimple application.

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

    Web web = new Web(serviceDefinition);
    Pet pet = web.clientOf(targetUrl + "/addPet/myPet")
                 .post(new Pet("pouetpouet"), Pet.class);

The Web class is constructed and can derive some information from a ServiceDefinition instance, which includes the content-type and accept headers.

Using the WebProxy class

Finally, you can also generate a client implementation using the WebProxy class. First you need to define an interface and annotate methods using the RestSimple annotation set.

    public static interface PetClient {

        @Get
        @Path("getPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet get(@PathParam("myPet") String path);

        @Post
        @Path("addPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet post(@PathParam("myPet") String myPet, String body);

        @Delete
        @Path("deletePet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet delete(@PathParam("myPet") String path);

    }

Then you can generate the client the implementation by simply doing:

    PetClient client = WebProxy.createProxy(PetClient.class, URI.create(targetUrl));
    Pet pet = client.post(new Pet("pouetpouet"), "myPet");