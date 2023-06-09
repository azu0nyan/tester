package controller.db.codecs

import org.bson.{BsonReader, BsonType, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.mongodb.scala.bson.ObjectId

class SomeCodec extends Codec[Some[_]] {
  override def encode(writer: BsonWriter, value: Some[_], encoderContext: EncoderContext): Unit = value match {
    case Some(v: String) => writer.writeString(v)
    case Some(v: Int) => writer.writeInt32(v)
    case Some(v: Long) => writer.writeInt64(v)
    case Some(v: Boolean) => writer.writeBoolean(v)
    case Some(v: ObjectId) => writer.writeObjectId(v)
  }

  override def getEncoderClass: Class[Some[_]] = classOf[Some[_]]

  override def decode(reader: BsonReader, decoderContext: DecoderContext): Some[_] = {
    reader.getCurrentBsonType match {
      case BsonType.BOOLEAN => Some(reader.readBoolean())
      case BsonType.STRING => Some(reader.readString())
      case BsonType.INT64 => Some(reader.readInt64())
      case BsonType.INT32 => Some(reader.readInt32())
      case BsonType.OBJECT_ID => Some(reader.readObjectId())
    }
  }
}
