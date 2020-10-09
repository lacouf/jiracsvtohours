# jiracsvtohours
Compiled with java 11

Gives a list of users with their worklog and totals from a specific date.

On Jira, got to Issues->Advanced Searched
Add column (under system) select column 'Time Spent'
Export Excel CSV All fields

mvn package

In the target directory
java -jar JiraCsv-1.0-SNAPSHOT-jar-with-dependencies.jar file.csv dd-MM-yyyy # of start of worklog
