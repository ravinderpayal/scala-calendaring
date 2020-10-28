package com.rapidfork.calendaring

import java.io.{FileNotFoundException, FileOutputStream, IOException}
import java.net.{URI, URISyntaxException}
import java.time.temporal.Temporal
import java.util.{Date, UUID}

import com.sendgrid.Request
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.parameter.{Cn, CuType, Role}
import net.fortuna.ical4j.model.property._
import net.fortuna.ical4j.validate.ValidationException
// import play.api.libs.json.{Json, OFormat}
import play.api.libs.json._


object SendMeetingInvite {

  def main(args: Array[String]) {
    val uid = UUID.randomUUID().toString + "@base-email.com"
    println(uid)
    val attendees = List(
      ProdugieGlaEventAttendee("U1", "u1@abc.xyz"),
      ProdugieGlaEventAttendee("U2 Testing", "u2@outlook.com"),
      ProdugieGlaEventAttendee("U3 Jazz", "jazz@example.com"),
      ProdugieGlaEventAttendee("U4 Vanilla", "vanilla@example.com"),
      ProdugieGlaEventAttendee("U5 King", "kong@island.com"),
      ProdugieGlaEventAttendee("U6 Party", "party@example.com")
    )
    invite(
      "sample@base-email.com",
      ProdugieGlaEvent(
        uid,
        "sample@base-email.com",
        "Test event2", "Test event summary-description", "Zoom",
        new Date(new Date().getTime + 10000000),
        new Date(new Date().getTime + 13600000),
        attendees,
        1
      )
    )

    cancel(
      "sample@base-email.com",
      ProdugieGlaEvent(
        uid,
        "sample@base-email.com",
        "Test event2", "Test event summary-description", "Zoom",
        new Date(new Date().getTime + 10000000),
        new Date(new Date().getTime + 13600000),
        attendees,
        2
      )
    )
  }

  def invite(email: String, event: ProdugieGlaEvent): Unit = {
    val calFile = "TestCalendar.ics"
    val calendar = new Calendar()
    calendar.add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"))
    calendar.add(Version.VERSION_2_0)
    calendar.add(CalScale.GREGORIAN)
    calendar.add(Method.REQUEST)

    val meeting = buildMeetingObject(event, Status.VEVENT_CONFIRMED)

    calendar.add(meeting)

    var fout: FileOutputStream = null

    try fout = new FileOutputStream(calFile)
    catch {
      case e: FileNotFoundException =>
        e.printStackTrace()
    }

    val outputter = new CalendarOutputter
    outputter.setValidating(false)

    try outputter.output(calendar, fout)
    catch {
      case e: IOException =>
        e.printStackTrace()
      case e: ValidationException =>
        e.printStackTrace()
    }
    val body = Json.obj(
      "personalizations" -> List(Json.obj(
        "to" -> Json.toJson(event.attendees),
        "subject" -> s"Meeting  : ${event.subject}"
      )),
      "from" -> Json.obj("email" -> "life@produgie.com"),
      "content" -> List(
        Json.obj(
          "type" -> "text/plain",
          "value" -> s"${event.description}"
        ),
        Json.obj(
          "type" -> "text/html",
          "value" -> "<strong>Please attend our event</strong>"
        ),
        Json.obj(
          "type" -> """text/calendar; method=REQUEST""",
          "method" -> "REQUEST",
          "encoding" -> "7bit",
          "value" -> calendar.toString
        ))
    )
    sendEmail(body.toString())

    // System.out.println(meeting)
  }

  def cancel(email: String, event: ProdugieGlaEvent): Unit = {
    val calFile = "TestCalendar.ics"

    val calendar = new Calendar()
    calendar.add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"))
    calendar.add(Version.VERSION_2_0)
    calendar.add(CalScale.GREGORIAN)
    calendar.add(Method.CANCEL)


    val meeting = buildMeetingObject(event, Status.VEVENT_CANCELLED)

    calendar.add(meeting)

    var fout: FileOutputStream = null

    try fout = new FileOutputStream(calFile)
    catch {
      case e: FileNotFoundException =>
        e.printStackTrace()
    }

    val outputter = new CalendarOutputter
    outputter.setValidating(false)

    try outputter.output(calendar, fout)
    catch {
      case e: IOException =>
        e.printStackTrace()
      case e: ValidationException =>
        e.printStackTrace()
    }
    // System.out.println(meeting)

    val body = Json.obj(
      "personalizations" -> List(Json.obj(
        "to" -> Json.toJson(event.attendees),
        "subject" -> s"Meeting  : ${event.subject}"
      )),
      "from" -> Json.obj("email" -> "life@test.rapidfork.com"),
      "content" -> List(
        Json.obj(
          "type" -> "text/plain",
          "value" -> s"${event.description}"
        ),
        Json.obj(
          "type" -> "text/html",
          "value" -> "<strong>Please attend our event</strong>"
        ),
        Json.obj(
          "type" -> """text/calendar; method=REQUEST""",
          "method" -> "REQUEST",
          "encoding" -> "7bit",
          "value" -> calendar.toString
        ))
    )
    sendEmail(body.toString())
    calendar
  }
  def sendEmail(body: String): Unit = {
    import com.sendgrid.{Method, SendGrid}
    val sg = new SendGrid("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")
    val request = new Request()
    request.setMethod(Method.POST)
    request.setEndpoint("mail/send")
    request.setBody(body) // charset=\"UTF-8\"\;
    // request.setBody("{\"personalizations\" ->[{\"to\" ->[{\"email\" ->\"test@example.com\"}],\"subject\" ->\"Sending with Twilio SendGrid is Fun\"}],\"from\" ->{\"email\" ->\"rav@test.rapidfork.com\"},\"content\" ->[{\"type\" ->\"text/plain\",\"value\" -> \"and easy to do anywhere, even with Java\"}]}")
    val response = sg.api(request)
    System.out.println(response.getStatusCode)
    System.out.println(response.getBody)
    System.out.println(response.getHeaders)

  }

  def buildMeetingObject(event: ProdugieGlaEvent, status: Status) = {
    val strTime: Temporal = event.startDateTime.toInstant
    val endTime = event.endDateTime.toInstant


    val meeting = new VEvent(
      strTime,
      // event.startDateTime,
      endTime, event.subject)

    meeting.add(
      new Uid(event.uid)
    )
    meeting.add(new Sequence(event.updateNumber))
    val org = new Organizer("mailto:" + event.organizer)
    org.add(new Cn(event.organizer))
    try meeting.add(org)
    catch {
      case e: URISyntaxException =>
        e.printStackTrace()
    }
    event.attendees.foreach(attendee => {
      val dev1 = new Attendee(URI.create(s"mailto:${attendee.email}"))
      dev1.add(Role.REQ_PARTICIPANT)
      // dev1.add()
      dev1.add(new Cn(attendee.name))
      dev1.add(CuType.INDIVIDUAL)
      meeting.add(dev1)
      None
    })

    meeting.add(status)

    meeting.add(Transp.OPAQUE)

    meeting.add(new Location(event.location))
    meeting.add(new Description(event.description))

    meeting
  }

  case class ProdugieGlaEventAttendee(name: String, email: String)

  case class ProdugieGlaEvent(
                               uid: String,
                               organizer: String,
                               subject: String,
                               description: String,
                               location: String,
                               startDateTime: Date,
                               endDateTime: Date,
                               attendees: List[ProdugieGlaEventAttendee],
                               updateNumber: Int)

  object ProdugieGlaEvent {
    implicit def formatEvent: OFormat[ProdugieGlaEvent] = Json.format[ProdugieGlaEvent]
  }

  object ProdugieGlaEventAttendee {
    implicit def formatAttendee: OFormat[ProdugieGlaEventAttendee] = Json.format[ProdugieGlaEventAttendee]
  }
}
