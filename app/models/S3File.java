package models;

import com.amazonaws.services.s3.model.CannedAccessControlList;
//import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.*;
import play.Logger;
import play.db.ebean.Model;
import plugins.S3Plugin;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.File;
import java.lang.Long;
import java.lang.String;
import java.lang.System;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Entity
public class S3File extends Model {

    @Id
    public UUID id;

    private String bucket;

    public String name;

    public Long userId;

    @Transient
    public File file;

    public Long getUserId() {
        return userId;
    }

    public static Finder<Long, S3File> find = new Finder<Long, S3File>(Long.class, S3File.class);

    public static List<S3File> findByUser(Long userId) {
        return find.where().eq("userId", userId).findList();
    }

    public URL getUrl() throws MalformedURLException {
        return new URL("https://s3.amazonaws.com/" + bucket + "/" + getActualFileName());
    }

    private String getActualFileName() {
        return id + "/" + name;
    }

    @Override
    public void save() {
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not save because amazonS3 was null");
            throw new RuntimeException("Could not save");
        }
        else {
            this.bucket = S3Plugin.s3Bucket;

            super.save(); // assigns an id

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, getActualFileName(), file);
            putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
            S3Plugin.amazonS3.putObject(putObjectRequest); // upload file
        }
    }

    @Override
    public void delete() {
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not delete because amazonS3 was null");
            throw new RuntimeException("Could not delete");
        }
        else {
            S3Plugin.amazonS3.deleteObject(bucket, getActualFileName());
            super.delete();
        }
    }

    public String download() {
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not download because amazonS3 was null");
            throw new RuntimeException("Could not download");
        }
        else {
            this.bucket = S3Plugin.s3Bucket;
            File localFile = new File(this.getActualFileName());
            ObjectMetadata object = S3Plugin.amazonS3.getObject(new GetObjectRequest(bucket, getActualFileName()), localFile);
            return this.getActualFileName();
        }
    }

}