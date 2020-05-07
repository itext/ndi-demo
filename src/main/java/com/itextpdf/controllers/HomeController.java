package com.itextpdf.controllers;

import com.google.common.base.Strings;
import com.itextpdf.adapters.ndi.auth.services.IAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final static Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final IAuthService authService;

    @Inject
    public HomeController(IAuthService authService) {
        this.authService = authService;
    }

    public CompletableFuture<Result> beat() {
        return CompletableFuture.completedFuture(ok());
    }

    public Result appLogin(String ndiId) {
        if (Strings.isNullOrEmpty(ndiId)) {
            return ok(com.itextpdf.views.html.login.render());
        }
        try {
            if (!authService.isAuthorized(ndiId)) {
                authService.login(ndiId);
            }
            return redirect(com.itextpdf.controllers.routes.HomeController.personalPage());
        } catch (CompletionException te) {
            logger.error("login failed ", te);
            return internalServerError("Login failed: No response from NDI API");
        } catch (Exception e) {
            logger.error("login failed ", e);
            return badRequest("Login failed: " + e.getMessage());
        }
    }

    public Result personalPage() {

        String ndiId   = session("connected");
        String message = Optional.ofNullable(flash("message")).orElse("");
        return ok(com.itextpdf.views.html.private_page.render(ndiId, message));
    }

    public Result appLogoff() {
        this.authService.logoff();
        return redirect(com.itextpdf.controllers.routes.HomeController.appLogin(null));
    }

    public Result index() {
        if (!authService.isAuthorized()) {
            return redirect(com.itextpdf.controllers.routes.HomeController.personalPage());
        } else {
            return redirect(com.itextpdf.controllers.routes.HomeController.appLogin(null));
        }
    }
}
