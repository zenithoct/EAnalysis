import org.apache.spark
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.SparkContext._
import org.jsoup.Jsoup

/**
  * Created by siva on 23/10/16.
  */

/**
  * This is the final stage of the data pipeline. This is used to do analysis on pst.json files.
  * For the sake of the test the spark application is run on local mode. This can be easily changed into
  * yarn cluster mode
  *
  */
object EnronAnalysis {

  /**
    * This accepts three arguments. First location of the json files, second avglength location where avg length email is saved
    * third argument is top100 location where top100 results will be saved
    * @param args
    *
    * For the sake of the test, spark application is run under local mode. this can be easily run on yarn cluster mode
    */
  def main(args: Array[String]): Unit = {
    //For the sake of the test, spark application is run under local mode. this can be easily run on yarn cluster mode
    // creating spark session
    val spark = SparkSession
      .builder()
      .appName("ENRON Email Analysis")
      .master("local")
      .getOrCreate()

    // checking for correct number of  arguments supplied
    if(args.length<3){
      println("Please provide 3 arguments")
    }

    val path = args(0)
    val avgLengthPath=args(1)
    val top100Path=args(2)
    //avgLengthPath="/home/siva/personal/inmail/outmail/output/myavg"
    //top100Path="/home/siva/personal/inmail/outmail/output/mycount"
    // reading json files
    val emailDF = spark.read.json(path)
    // creating dataframe
    emailDF.createOrReplaceTempView("emails")
    // calculting avglength
    avgLength(emailDF,spark,avgLengthPath)
    // calculating Top100Recipient
    Top100Recipient(emailDF,spark,top100Path)


    spark.stop


  }

  /**
    *
    * @param emailDF: emaildataframe
    * @param spark : spark session
    * @param avgLengthPath :
    *
    *   x-Tika:Content column have data in html. to extract email body, we need to extract html body tag, which is done using Jsoup html parser
    *
    */
  def avgLength(emailDF:DataFrame,spark:SparkSession,avgLengthPath:String):Unit = {

    import spark.implicits._
    // exploding array column into rows
    val emailsContent = spark.sql("SELECT identifier,explode(`X-TIKA:content`) FROM emails ").toDF("emailId","content")


    // extract html content and find length
    val emailBodyContent=emailsContent.map(x=>extractBody(x.getString(1)).length).toDF("ContentLength")
    emailBodyContent.createOrReplaceTempView("emailBodyContent")


   // calcultaing avg
   val result=spark.sql("select sum(ContentLength)/count(ContentLength) as avgEmailLength from emailBodyContent ")
    // repartition into 1 so single file is created when saving data frame
    result.repartition(1)
      .write
      .format("com.databricks.spark.csv")
      .option("header", "true")
      .save(avgLengthPath)

  }

  /**
    * using Jsoup parser extract html body
    * @param html
    * @return
    */
  def extractBody(html:String):String={
         Jsoup.parse(html).body().text()
  }

  /**
    * used find top 100 recipients. ccemail are given only 50% weightage
    * @param emailDF
    * @param spark
    * @param top100Path
    */

  def Top100Recipient(emailDF:DataFrame,spark:SparkSession,top100Path:String):Unit = {

    import spark.implicits._
    val toEmails = spark.sql("SELECT explode(displayTo) FROM emails ").toDF("ToEmails")

    val ccEmails = spark.sql("SELECT explode(displayCc) FROM emails ").toDF("CcEmails")

    val to= toEmails.map(x=>x.getString(0).split(';')).toDF("To")
    to.createTempView("To")
    val toCount= spark.sql("SELECT explode(To),1 FROM To ").toDF("ToEmails","Counts")
    val toEmailAggDF = toCount.groupBy("ToEmails").count.toDF("Emails","Counts")


    val cc= ccEmails.map(x=>x.getString(0).split(';')).toDF("cc")
    cc.createTempView("cc")
    val ccCount= spark.sql("SELECT explode(cc),1 FROM cc ").toDF("CCEmails","Counts")



    val countWeightage = new CountWeightage[(String,Int)](x=>1.0*0.5).toColumn


    // ccemails are given only 50% weightage
    val ccEmailAggDF= ccCount.as[(String,Int)].groupByKey(_._1).agg(countWeightage).toDF("Emails","Counts")

    // saving to a file
    toEmailAggDF.union(ccEmailAggDF).groupBy("Emails").sum().toDF("Emails","Count").sort($"Count".desc).limit(100)
      .repartition(1)
      .write
      .format("com.databricks.spark.csv")
      .option("header", "true")
      .save(top100Path)


  }


}
