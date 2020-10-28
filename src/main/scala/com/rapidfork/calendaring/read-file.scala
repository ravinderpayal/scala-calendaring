package com.rapidfork.calendaring

import java.io.{FileInputStream, StringReader}
import java.time.Instant

import net.fortuna.ical4j.data.{CalendarBuilder, UnfoldingReader}
import net.fortuna.ical4j.model.component._
import net.fortuna.ical4j.model.property._
import org.apache.james.mime4j.codec.QuotedPrintableInputStream
import org.apache.james.mime4j.dom.Multipart
import org.apache.james.mime4j.field.mimeversion.parser.MimeVersionParserTokenManager
import org.apache.james.mime4j.message.MultipartBuilder
import org.apache.james.mime4j.stream.{EntityState, MimeTokenStream}

import scala.io.Source
import scala.jdk.CollectionConverters._

object FileRead {


  def main(args: Array[String]) {

    val fname = "email-5.txt"
    val fSource = Source.fromFile(fname)
    val stream = new MimeTokenStream()
    stream.parse(new FileInputStream("email-5.txt"));
    println(stream.getState);
    //println(stream.getInputStream)
    while (
      stream.next() != EntityState.T_END_OF_STREAM
    ) {
      val state = stream.getState()
      state match {
        case EntityState.T_START_HEADER =>
        //println("Start Header detected")

        case EntityState.T_BODY =>
          // println("Body detected, contents = "
          //   + stream.getInputStream() + ", header data = ")
          val mimeType = stream.getBodyDescriptor().getMimeType()
          // println("mime type=")

          if (mimeType == "application/ics") {
            // println("ics file detected")

            val ics = " " + stream.getDecodedInputStream.readAllBytes().map(_.toChar).mkString + "\n"
            processCalendar(ics)
          }

          // println("getBodyDescriptor=" + stream.getBodyDescriptor);

          // println("getConfig=" + stream.getConfig);

          // println("getDecodedInputStream=" + stream.getDecodedInputStream);

          // println("getReader=" + stream.getReader);

        //  + stream.getBodyDescriptor())

        case EntityState.T_FIELD =>
        //println("Header field detected: "
        //  + stream.getField())
        case EntityState.T_START_MULTIPART =>
          println("Media type: " + stream.getBodyDescriptor.getMediaType)
          println("Mime type: " + stream.getBodyDescriptor.getMimeType)
          val multipartBuilder = MultipartBuilder.create().addBinaryPart(
            stream.getDecodedInputStream.readAllBytes(),
            stream.getBodyDescriptor.getMimeType)

          // multipartBuilder.getBodyParts

          val multipart = multipartBuilder.build()
          println(stream.getDecodedInputStream)
          println(multipart.getBodyParts.asScala.map(_.getMimeType).mkString("\n-----\n-----\n"))
        /*println("Multipart message detexted,"
          + " header data = "
          + stream.getBodyDescriptor())*/
        case EntityState.T_END_HEADER =>
        // println("End Header detected")
        case EntityState.T_PREAMBLE =>
        // println("Preamble detected")
        case EntityState.T_START_MESSAGE =>
          println(stream.getInputStream)
        case _ =>
          println(state.toString)
      }
    }
  }

  def processCalendar(ics: String): Unit = {
    println("\n\n\n----------\n\n\n")
    val builder = new CalendarBuilder()

    val reader = new UnfoldingReader(new StringReader(ics))
    reader.read()

    val calendar = builder.build(reader)
    val cs = calendar.getComponents()
    // println(calendar)
    // for ( c <- cs.getAll.iterator().asScala) {
    println(calendar.getProperty[Method]("METHOD").map(_.getValue))
    val c = cs.getAll.iterator().next()

    if (c.isInstanceOf[VEvent]) {
      // println(c)
      // val organizer = c.getProperties.get("Organiser").get(0)
      // println(organizer)

      for (p <- c.getProperties().getAll.asScala) {
        // println(p.getName() + " : " + p.getValue());
        //println(":::::")
        p match {
          case method: Method =>
            println("Method: "  + method.getValue)
          case attendee: Attendee =>
            println("Attendee: " + getEmail(attendee.getValue))
          case organizer: Organizer =>
            println("Organizer: " + getEmail(organizer.getValue))
          case uid: Uid =>
            println("UID: " + uid.getValue)
          case status: Status =>
            println("Status: " + status.getValue)
          case location: Location =>
            println("Location: " + location.getValue)
          case summary: Summary =>
            println("Summary: " + summary.getValue)
          case description: Description =>
            println("Description: " + description.getValue)
          case dtStart: DtStart[Instant] =>
            println("Starts at: " + dtStart.getDate.toString)
          case dtEnd: DtEnd[Instant] =>
            println("Ends at: " + dtEnd.getDate.toString)
          case _ =>
        }
        /*        if (p.isInstanceOf[Attendee]) {
                  val attendee = p.asInstanceOf[Attendee]
                  println(attendee.getCalAddress)

                  for (pa <- p.getParameters().getAll.asScala) {
                    println(":::::")
                    println(pa.getValue())
                    println(":::::")
                  }
                } else if (p.isInstanceOf[Organizer]) {
                  println(p.asInstanceOf[Organizer].getCalAddress)
                  println("organizer found")
                }*/
        println("------")
      }
    }
    // }
  }

  def getEmail(address: String): String = {
    address.replace("mailto:", "")
  }
}