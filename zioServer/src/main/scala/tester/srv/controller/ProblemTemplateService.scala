package tester.srv.controller

import clientRequests.admin.CustomProblemUpdateData
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import otsbridge.ProblemTemplate
import zio.*

trait ProblemTemplateService {

  def newSimpleTemplate(alias: String, title: String, html: String): TranzactIO[Boolean]

  def removeTemplateCascade(alias: String): TranzactIO[Boolean]

  def updateTemplate(alias: String, updateData: CustomProblemUpdateData): TranzactIO[Boolean]
}
