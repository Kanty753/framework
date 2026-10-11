package app.controllers;

import java.io.IOException;

import annotation.Controller;
import annotation.UrlMapping;
import annotation.WebApi;
import app.models.Personne;
import app.models.User;
import jakarta.servlet.http.*;
import utils.ModelAndView;

@Controller
public class HomeController {

    @UrlMapping(url = "/home", method = "GET")
    public void homes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().println("Bonjour ");
    }

    @UrlMapping(url = "/test")
    public String test() {
        return "Test OK";
    }

    @UrlMapping(url = "/index")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addAttribute("message", "Bonjour depuis le contrôleur !");
        return modelAndView;
    }

    @UrlMapping(url = "/api/hello")
    @WebApi
    public String hello() {
        return "Bonjour depuis l'API";
    }

    @UrlMapping(url = "/api/user")
    @WebApi
    public User getUser() {
        return new User("Aina", "aina@itu.mg");
    }

    // Sprint 7 — binding de paramètres simples
    @UrlMapping(url = "/salut", method = "POST")
    public ModelAndView salut(String nom, int age) {
        ModelAndView mv = new ModelAndView("salut");
        mv.addAttribute("nom", nom);
        mv.addAttribute("age", age);
        return mv;
    }

    // Sprint 7-bis étape 1 — binding d'objet simple
    @UrlMapping(url = "/salut2", method = "POST")
    public ModelAndView salut2(Personne p) {
        ModelAndView mv = new ModelAndView("salut2");
        mv.addAttribute("p", p);
        return mv;
    }

    // Sprint 7-bis étape 2 — binding de deux objets imbriqués
    @UrlMapping(url = "/salut3", method = "POST")
    public ModelAndView salut3(Personne p1, Personne p2) {
        ModelAndView mv = new ModelAndView("salut3");
        mv.addAttribute("p1", p1);
        mv.addAttribute("p2", p2);
        return mv;
    }
}