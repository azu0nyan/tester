package controller

import DbViewsShared.CourseShared.{AnswerStatus, CourseStatus}
import DbViewsShared.GradeRule.{GradeRound, GradedProblem}
import DbViewsShared.{GradeOverride, GradeRule}
import com.typesafe.scalalogging.Logger
import controller.db.CustomCourseTemplate
import controller.db.codecs.{DisplayMeCodecProvider, OptionCodec, SomeCodec}
import org.bson.codecs.Codec
import org.bson.types.ObjectId
import org.mongodb.scala.{ClientSession, Completed, MongoClient, MongoCollection, MongoDatabase, Observable, Observer, ReadConcern, SingleObservable, TransactionOptions, WriteConcern}
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromProviders, fromRegistries}
import org.slf4j.LoggerFactory
import otsbridge.CoursePiece.CoursePiece
import otsbridge.ProblemScore.ProblemScore
import otsbridge.ProgramRunResult.ProgramRunResult
import otsbridge.{DisplayMe, ProblemScore, ProgramRunResult}

import scala.reflect.ClassTag
//import otsbridge.{ProblemScore, ProgramRunResult}


package object db extends CollectionOps {

  val log: Logger = Logger(LoggerFactory.getLogger("db"))

  trait MongoObject {
    val _id: ObjectId

    def updatedFromDb[T](implicit col: MongoCollection[T], c: ClassTag[T]): T = CollectionOps(col).byId(_id).get
  }

  val dbName = "myTestDb"

  //  import org.mongodb.scala.bson.codecs.Macros
  //  val problemScoreCodecProvider = Macros.createCodecProvider[ProblemScore]()

  val codecRegistry = fromRegistries(fromProviders(
    classOf[User],
    classOf[UserRole],
    classOf[Group],
    classOf[Problem],
    classOf[Answer],
    classOf[Course],
    classOf[CourseStatus],
    classOf[ProgramRunResult],
    classOf[ProblemScore],
    classOf[UserToGroup],
    //    mongoHelper.problemRunResultCodecProvider,
    //    mongoHelper.problemScoreCodecProvider,
    classOf[AnswerStatus],
    //    classOf[ProblemSetScore],
    classOf[CourseTemplateForGroup],
    classOf[CourseTemplateAvailableForUser],


    classOf[GradeOverride],
    classOf[GradeRound],
    classOf[GradedProblem],
    classOf[GradeRule],
    classOf[Grade],
    classOf[GroupGrade],

    DisplayMeCodecProvider,


    classOf[CoursePiece],


    classOf[CustomCourseTemplate],

    //todo separate
    classOf[LtiProblem],
    classOf[LtiConsumerKey],

  ), fromCodecs(
    new SomeCodec,
//    new OptionCodec(),
//    new OptionCodec().asInstanceOf[Codec[None.type]]
  ), DEFAULT_CODEC_REGISTRY)

  val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase(dbName).withCodecRegistry(codecRegistry)

  implicit val users: MongoCollection[User] = database.getCollection("users")
  implicit val groups: MongoCollection[Group] = database.getCollection("groups")
  implicit val userToGroup: MongoCollection[UserToGroup] = database.getCollection("userToGroup")
  implicit val answers: MongoCollection[Answer] = database.getCollection("answers")
  implicit val problems: MongoCollection[Problem] = database.getCollection("problems")
  implicit val courses: MongoCollection[Course] = database.getCollection("courses")
  implicit val coursesAvailableForUser: MongoCollection[CourseTemplateAvailableForUser] = database.getCollection("coursesAvailableForUser")
  implicit val courseTemplateForGroup: MongoCollection[CourseTemplateForGroup] = database.getCollection("CourseTemplateForGroup")
  implicit val customCourseTemplates: MongoCollection[CustomCourseTemplate] = database.getCollection("CustomCourseTemplate")
  implicit val grades: MongoCollection[Grade] = database.getCollection("grades")
  implicit val groupGrades: MongoCollection[GroupGrade] = database.getCollection("groupGrades")
  //todo separate
  implicit val ltiProblems: MongoCollection[LtiProblem] = database.getCollection("ltiProblem")
  implicit val ltiConsumerKeyToSharedSecrets: MongoCollection[LtiConsumerKey] = database.getCollection("consumerKeyToSharedSecret")

}
