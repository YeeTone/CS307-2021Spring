package reference.util;

import cn.edu.sustech.cs307.config.Config;
import cn.edu.sustech.cs307.factory.ServiceFactory;

public abstract class Util {
    protected static final ServiceFactory SERVICE_FACTORY= Config.getServiceFactory();

    public Util() {
        throw new RuntimeException("No Util Instance for you!");
    }
}
