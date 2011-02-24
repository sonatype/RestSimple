The REST Deflector Framework allow programmatic creation of REST resource that can be deployed inside a variety of framework like jax-rs, Sitebricks, etc. The main idea is to able to define Service Definition, which gets translated into specific framework resource native code.

        Injector injector = Guice.createInjector(new SitebricksModule(binder()));

        Action action = new PetstoreAction();
        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition
                .withHandler(new GetServiceHandler("getMyPet", action).consumeWith(JSON, Pet.class).producing(JSON))
                .withHandler(new PostServiceHandler("postMyPet", action).consumeWith(JSON, Pet.class).producing(JSON))
                .bind();

Request will be delegated to the {@link Action}'s method using the information contained within
{@link ServiceHandler}. Using the example above, a request to:

     POST /postMyPet/myPet

     { "name" : "pouet" }

will be mapped on the server side to the ServiceHandler defined as

    PostServiceHandler

which will invoke its associated Action implementation

    PetstoreAction.action(ActionContext)

The ActionContext.get() will return a POJO called Pet, which is simply:

public class Pet {

    public String name;

    public Pet() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

Currently we support SiteBricks and any JAXRS implementation like Jersey, RESTEasy, etc.






