/**
  * Created by siva on 23/10/16.
  */
import java.io.{ByteArrayInputStream, File, FileOutputStream, IOException}
import java.util.zip.ZipInputStream

import org.apache.spark
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.SparkContext._
import org.apache.spark.input.PortableDataStream
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest}
import com.amazonaws.util.IOUtils


/** This is first part of the data pipeline. This will unzip enron zip files into pst files in destination directory
  * Arguments to be supplied. For purpose of test program is run local. This can be easily changed to run on Yarn Cluster
  *
  *  arg(0): Input path  where enron zip files reside
  *  arg(1): FileName pattern using * for eg EDRM-Enron-PST-0*.zip
  *  arg(2): output path where pst files will be saved
  */

object UnzipFiles {

  def main(args: Array[String]): Unit = {
    // for purpose of test, running spark in local mode. This can be easily changed to run on Yarn Cluster

    val conf = new SparkConf().setAppName("Unzip file").setMaster("local")
    // creating spark context which is the entry point for the application
    val sc=new SparkContext(conf)

    // checking if correct arguments has been sent
    if(args.length<3){
      println("Please provide 3 arguments")
    }
    val inputPath=args(0)
    // inputPath="/home/ubuntu/testdata/edrm-enron-v1/"
    val fileName=args(1)
    // fileName="EDRM-Enron-PST-0*.zip"

    val outputPath=args(2)
    // outputPath= "/home/siva/personal/inmail/outmail"
    // reading files as binary
    val files=sc.binaryFiles("file://"+ inputPath +"/"+fileName,1)

   // val files=sc.binaryFiles("file:///home/siva/personal/inmail/EDRM-Enron-PST-0*.zip",1)
    // unzipping each files
    files.foreach(x=>unZipFiles(x._2,outputPath))

    sc.stop()

  }
  /*
   * Unzipping unmodule which takes portable datastream and destination path where unzipped file is saved
   *

   */
  def unZipFiles(content:PortableDataStream,destination:String): Unit =
  {

    val zippedFiles= new ZipInputStream(content.open)

     //val destination = "/home/siva/personal/inmail/outmail"
     val buffer = new Array[Byte](1024)
    try {
      // getting each file from zip
      var entry = zippedFiles.getNextEntry
      while (entry != null) {
        // getting the name of the file
        val fileName = entry.getName
        // uploadS3(IOUtils.toByteArray(zippedFiles),fileName)
        // saving to new file at destination path
        val newFile = new File(destination + File.separator + fileName)

         println("file unzip : " + newFile.getAbsoluteFile())

        //create folders
        new File(newFile.getParent()).mkdirs()

        val fos = new FileOutputStream(newFile)

        var len: Int = zippedFiles.read(buffer)


        while (len > 0) {

          fos.write(buffer, 0, len)
          len = zippedFiles.read(buffer)
        }

        fos.close()
        entry = zippedFiles.getNextEntry()
      }


    }
    catch {
      case e: IOException => println("exception caught: " + e.getMessage)
    }
    finally
    {
      zippedFiles.closeEntry()
      zippedFiles.close()
    }
  }
  def uploadS3(content:Array[Byte],fileName:String): Unit =
  {
    val AWS_ACCESS_KEY = "AKIAJ5EMZXVXGQBRXBVQ"
    val AWS_SECRET_KEY = "BEQ1yYjBvWOdrFJZgWhsgcLQj/yW4z7hesO9lzPR"

    val yourAWSCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
    val amazonS3Client = new AmazonS3Client(yourAWSCredentials)
    val objectMetadata = new ObjectMetadata()
    objectMetadata.setContentLength(content.length)
    val contentStream =new ByteArrayInputStream(content)
    try
    {
      amazonS3Client.putObject("enronpst", fileName, contentStream, objectMetadata)
    }
    catch
    {
        case e: IOException => println("exception caught: " + e.getMessage)
    }
    finally
    {
      if(contentStream !=null)
        contentStream.close()

    }



  }


}
