package controllers;

import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http;
import play.mvc.Security;
import views.html.restricted;
import views.html.fileupload;
import views.html.fileready;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

import static akka.pattern.Patterns.ask;
import akka.dispatch.Future;
//import akka.actor.UntypedActorFactory;

import com.typesafe.config.ConfigFactory;

import models.S3File;
import models.User;
import actors.FileReadActor;

@Security.Authenticated(Secured.class)
public class FileUpload extends Controller {

    public static Result index() {
        // show all files
        // List<S3File> s3Files = new Model.Finder(UUID.class, S3File.class).all();

        // show the files uploaded by current user
        final User localUser = Application.getLocalUser(session());
        List<S3File> s3Files = S3File.findByUser(localUser.getId());

        return ok(fileupload.render(s3Files));
    }

    public static Result upload() {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("upload");
        if (uploadFilePart != null) {
            final User localUser = Application.getLocalUser(session());
            S3File s3File = new S3File();
            s3File.userId = localUser.getId();
            s3File.name = uploadFilePart.getFilename();
            s3File.file = uploadFilePart.getFile();
            s3File.save();

            // generate and save tags
            String fileId = s3File.download();
            generateTag(fileId);

//            return redirect(routes.FileUpload.index());
            return redirect(routes.FileUpload.ready());
        }
        else {
            return badRequest("File upload error");
        }
    }

    public static Result ready() {
        return ok(fileready.render("Your file is uploaded!"));
    }

    private static void generateTag(String fileId) {

        ActorSystem system = ActorSystem.create("WordCountApp");

        ActorRef fileReadActor = system.actorOf(new Props(FileReadActor.class), "fileread");

        fileReadActor.tell(fileId);
    }

}