package controllers;

import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http;
import play.mvc.Security;
import views.html.restricted;
import views.html.fileupload;

import java.lang.Exception;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;
import java.util.List;
import java.util.UUID;

import com.typesafe.config.ConfigFactory;

import models.S3File;
import models.User;


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

            String localFileName = s3File.download();
//            generateTag(localFileName);

            return redirect(routes.FileUpload.index());
        }
        else {
            return badRequest("File upload error");
        }
    }


}