Design
1. Data pipeline has three stages
   a. Unzip Stage: First extract enron email zip files using spark
   b. Parse Stage: parse the pst file using apache tika parser and spark and save the content into json file
   c. Analysis Stage: final stage is analysis stage in which spark program read json files and calculate avglength and top100 recipients and save results into files

2. Use following main technologies used
   a. Spark
   b. apache tika
   c. jsoup html parser
   d. scala

Run 

 1. mkdir testdata

 2. To mount enron file drive after attaching

    sudo mount /dev/xvdf /home/ubuntu/testdata -t ext4
 3. mkdir ~/project
 4. mkdir ~/project/pst
 5. mkdir ~/project/pstjson
 6. mkdir ~/project/avglength
 7. mkdir ~/project/top100
 8. upload  EnronAnalysis-assembly-1.0.jar file to ~/project
  
 
Run Data Pipeline

 1. Unzip stage: 
    spark-submit --class UnzipFiles --master local --driver-memory 160g --executor-memory 40g --executor-cores 5 --num-executors 4  /home/ubuntu/project/EnronAnalysis-assembly-1.0.jar "/home/ubuntu/testdata/edrm-enron-v1/" "EDRM-Enron-PST-0*.zip" "/home/ubuntu/project/pst"

 2. Tika Parse Stage:
 spark-submit --class EnronPSTParse --master local --driver-memory 160g --executor-memory 40g --executor-cores 5 --num-executors 4  /home/ubuntu/project/EnronAnalysis-assembly-1.0.jar "/home/ubuntu/project/pst/" "/home/ubuntu/project/pstjson/"

 3. Analysis Stage:
spark-submit --class EnronAnalysis --master local --driver-memory 160g --executor-memory 40g --executor-cores 5 --num-executors 4  /home/ubuntu/project/EnronAnalysis-assembly-1.0.jar "/home/ubuntu/project/pstjson/*.json" "/home/ubuntu/project/avglength" "/home/ubuntu/project/top100"



Alternative Design(less code)
1. Can use Lucid Fusion to parse and index the pst files
2. Query the index using spark


Future Enhancements:
1. Run data piple using oozie spark action.
2. Unit test cases


Assumptions

1. For the sake of test, spark application is run under local mode. This can be easily changed to Yarn cluster
2. CCemails are given only 50% weightage

