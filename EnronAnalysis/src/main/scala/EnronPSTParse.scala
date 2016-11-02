import java.io.{InputStream, _}
import java.nio.file.{Files, OpenOption}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.tika.metadata.Metadata

import org.apache.tika.parser.{AutoDetectParser, ParseContext, Parser, RecursiveParserWrapper}
import org.apache.tika.sax.{BasicContentHandlerFactory, WriteOutContentHandler, XHTMLContentHandler}
import org.json.JSONObject
import org.xml.sax.helpers.DefaultHandler


import scala.tools.nsc.interpreter.InputStream


/**
  * Created by siva on 23/10/16.
  */
/**
  *  Using Apache Tika to parse the pst file in the input path and once the document is parsed,
  *  metadata is collected and saved to json file in the destination folder. This is the second stage in the data pipeline
  *
  *  This takes two argument parameters. 1. Input path where pst file reside and 2. destination path where json file is stored.
  *  For the sake of the test, spark is run on local mode. This can be easily changed to run under yarn cluster
  */
object EnronPSTParse {

  def main(args: Array[String]): Unit = {
    //For the sake of the test, spark is run on local mode. This can be easily changed to run under yarn cluster
    val conf = new SparkConf().setAppName("EnronPSTParse").setMaster("local")
    // creating spark context
    val sc=new SparkContext(conf)
    // checking for correct number of arguments
    if(args.length<2){
      println("Please provide 2 arguments")
    }
    val inputfilePath= args.head
    //inputfilePath="/home/siva/personal/inmail/outmail/"
    val outputPath=args(1)
    //outputPath="/home/siva/personal/inmail/outmail/parsed/"
    // reading all file with one partition
    val files=sc.wholeTextFiles(inputfilePath+"*.pst",1)
      // processing each file and saving to output path
      files.foreach(x=>processTika(x._1,outputPath))

    sc.stop()

  }

  /**
    *  Take each filePath, reading content and parse using tika api
    * @param fileName: pst files location
    * @param outputPath: destination path
    */

  def processTika(fileName:String,outputPath:String):Unit={

    val p:Parser = new AutoDetectParser()
    // pst has folder, so need to use recursive wrapper to parse all the folders
    val wrapper = new RecursiveParserWrapper(p,
      new BasicContentHandlerFactory(
        BasicContentHandlerFactory.HANDLER_TYPE.HTML,-1));

    val context:ParseContext = new ParseContext();

    try {


      val fsname = fileName.split('/').last
      val fileoutName = outputPath + fsname + ".json"

      val is: InputStream = new FileInputStream(fileName)
      // parse using Default hanlder and read metadata
      wrapper.parse(is, new DefaultHandler, new Metadata(), context)
      val iter = wrapper.getMetadata.iterator()
      var json = new JSONObject
       // saving all metadata into json object
      while (iter.hasNext()) {

        val metadata: Metadata = iter.next()
        for (name <- metadata.names()) {
          for (value <- metadata.getValues(name) ) {
            json.append(name,value)
          }
        }

      }

     // uploadS3(json.toString.getBytes,fsname + ".json")
      // save to file in destination path
      val file2 = new FileWriter(fileoutName)
      file2.write(json.toString)
      file2.flush()
      file2.close()


    }
    catch{
      case e:Exception => println("error "+e.getMessage)
    }

  }




}
