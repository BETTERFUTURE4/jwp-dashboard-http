package servlet;

import java.util.List;
import nextstep.jwp.controller.LoginController;
import nextstep.jwp.controller.RegisterController;
import nextstep.jwp.controller.WelcomeController;
import nextstep.jwp.controller.exception.MethodNotAllowedHandler;
import nextstep.jwp.controller.exception.NotFoundHandler;
import nextstep.jwp.controller.exception.UnauthorizedHandler;
import nextstep.jwp.service.UserService;
import servlet.handler.Controller;
import servlet.handler.ExceptionHandler;

public class Config {

    private static final Config CONFIG = new Config();

    private final UserService userService = new UserService();

    private final List<Controller> controllers =
            List.of(new WelcomeController(),
                    new LoginController(userService), new RegisterController(userService));
    private final List<ExceptionHandler> exceptionHandlers =
            List.of(new NotFoundHandler(), new UnauthorizedHandler(), new MethodNotAllowedHandler());

    private Config() {
    }

    public static Config get() {
        return CONFIG;
    }

    public List<Controller> getControllers() {
        return controllers;
    }

    public List<ExceptionHandler> getExceptionHandlers() {
        return exceptionHandlers;
    }
}
