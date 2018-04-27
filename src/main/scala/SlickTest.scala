import slick.jdbc.MySQLProfile.api._
import com.typesafe.config._
import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import slick.jdbc.GetResult
import slick.sql.SqlStreamingAction

object SlickTest extends App {
  val db = Database.forConfig("mysql")

  val dbio = for {
    _ <- cleanDbIO
    _ <- generateTestData(100)
    result <- insertData
  } yield result

  val result = Await.result(db.run(dbio), Duration.Inf)
  result.map{case (v1, v2, v3) => (v1.head, v2.head, v3.head)}.foreach{case (i1, i2, i3) => println(s"foo: ${i1}, bar: $i2, baz: $i3")}

  def insertData : DBIOAction[Seq[(Vector[Long], Vector[Long], Vector[Long])], NoStream, Effect] = for {
    dataList <- getInput()
    list1 <- insertFoo(dataList)
    list2 <- insertBar(dataList)
    list3 <- insertBaz(dataList)
  } yield list1.zip(list2).zip(list3).map{case ((a,b), c) => (a, b, c)}

  def generateTestData(n: Int) : DBIOAction[List[Int], NoStream, Effect] = DBIO.sequence(
    (1 to n).toList.map(i => Data(s"foo$i", s"bar$i", s"baz$i")).map(d => sqlu"""insert into input(foo,bar,baz) values(${d.foo}, ${d.bar}, ${d.baz})""")
  )

  def cleanDbIO : DBIOAction[Seq[Int], NoStream, Effect] = DBIO.sequence(Seq(
    sqlu"""truncate table input""",
    sqlu"""truncate table foo""",
    sqlu"""truncate table bar""",
    sqlu"""truncate table baz""",
  ))

  def getInput() : SqlStreamingAction[Vector[Data], Data, Effect] = {
    import Data.conversion
    sql"select foo,bar,baz from input".as[Data]
  }

  def insertFoo(dataList: Seq[Data]) : DBIOAction[Seq[Vector[Long]], NoStream, Effect] = {
    DBIO.sequence(dataList.map{data => 
      for {
        _ <- sqlu"""insert into foo(foo) values(${data.foo})"""
        id <- sql"""select last_insert_id()""".as[Long]
      } yield(id)    
    })
  }
  def insertBar(dataList: Seq[Data]) : DBIOAction[Seq[Vector[Long]], NoStream, Effect] = {
    DBIO.sequence(dataList.map{data => 
      for {
        _ <- sqlu"""insert into bar(bar) values(${data.bar})"""
        id <- sql"""select last_insert_id()""".as[Long]
      } yield(id)    
    })
  }
  def insertBaz(dataList: Seq[Data]) : DBIOAction[Seq[Vector[Long]], NoStream, Effect] = {
    DBIO.sequence(dataList.map{data => 
      for {
        _ <- sqlu"""insert into baz(baz) values(${data.baz})"""
        id <- sql"""select last_insert_id()""".as[Long]
      } yield(id)    
    })
  }
}

case class Data(foo: String, bar: String, baz: String)
object Data {
  implicit val conversion : GetResult[Data] = GetResult(r => Data(r.<<, r.<<, r.<<))
}