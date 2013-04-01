# File Upload to AWS S3 and TagClouds  #

## Notes ##
1. User can create an account/log-in via OAuth authentication.  Currently accepting only google, but others (foursquare, twitter, linkedin, facebook...) can be added later.  
2. Once logged in, user can upload a file to AWS S3.
3. Upon file upload is complete, your is taken to the confirmation page that the file is uploaded and ready in AWS S3.
4. Assuming it's a text file, tags will be generated and persisted. (currently upto 5 top tags persisted but configurable)
5. For generating tag, MapReduce pattern is used. See below for details.

## Use of Akka Actors implementing MapReduce pattern ##
1. A FileReadActor is created upon upload, reads file line by line, send each line of text as a String message to a MasterActor
(For consistent performance, only first N lines of the file is being read for tag generation.)
2. A MasterActor reads the message, maps the words, reduces the words and finally does an inmemory aggregation of the result.
3. All event is non-blocking message flow. After the FileReadActor completing send the String message to the MasterActor, FileReadActor send CountResult message to the MasterActor demanding the result, and this CountResult will be populated by MasterActor(AggregateActor) and delivered back to FileReadActor. 
4. When FileReadActor receives the CountResult back, it persist the tag as JSON String in DB accordingly.

## Considerations for future improvements ##
1. Backend (download file) - downloading to InputStream instead of temporary File? 
2. Backend (word count) - Use remote actors for scalablity. Use routers (esp. if reading whole file instead of first N lines) for regulating the load.
3. UI - Upon tag generation, use Websocket to update the tag info in the browser w/o redirecting to confirmation page.

## References ##
Simple OAuth(2.0) authentication
https://github.com/joscha/play-authenticate/tree/master/samples/java

AWS S3 file upload
https://devcenter.heroku.com/articles/using-amazon-s3-for-file-uploads-with-java-and-play-2

MapReduce pattern with Akka
https://github.com/write2munish/Akka-Essentials/tree/master/FirstAkkaApplication
