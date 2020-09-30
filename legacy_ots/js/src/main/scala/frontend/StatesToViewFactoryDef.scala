package frontend

import frontend.views.{AdminActionsPageViewFactory, AdminCourseTemplateInfoPageViewFactory, AdminCoursesPageViewFactory, AdminGroupInfoPageViewFactory, AdminGroupListPageViewFactory, AdminPageViewFactory, AdminProblemsPageViewFactory, AdminUserListPageViewFactory, CoursePageViewFactory, CourseSelectionPageViewFactory, ErrorPageViewFactory, GroupGradesPageViewFactory, GroupScoresPageViewFactory, LandingPageViewFactory, LoginPageViewFactory, RegistrationPageViewFactory, RootPageViewFactory, TeacherConfirmAnswersPageViewFactory}
import io.udash.core.{ViewFactory, ViewFactoryRegistry}

class StatesToViewFactoryDef extends ViewFactoryRegistry[RoutingState] {
  override def matchStateToResolver(state: RoutingState): ViewFactory[_ <: RoutingState] = {
    val res = state match {
      case AdminActionsPageState => AdminActionsPageViewFactory
      case AdminCoursesPageState => AdminCoursesPageViewFactory
      case AdminCourseTemplateInfoPageState(_) =>AdminCourseTemplateInfoPageViewFactory
      case AdminGroupInfoPageState(_) => AdminGroupInfoPageViewFactory
      case AdminGroupListPageState => AdminGroupListPageViewFactory
      case AdminPageState => AdminPageViewFactory
      case AdminProblemsPageState => AdminProblemsPageViewFactory
      case AdminUserListPageState=> AdminUserListPageViewFactory
      case CoursePageState(courseId, taskId) => CoursePageViewFactory
      case CourseSelectionPageState => CourseSelectionPageViewFactory
      case LandingPageState => LandingPageViewFactory
      case LoginPageState => LoginPageViewFactory
      case RegistrationPageState => RegistrationPageViewFactory
      case RootState => RootPageViewFactory
      case TeacherConfirmAnswersPageState(_, _) => TeacherConfirmAnswersPageViewFactory
      case GroupGradesPageState(_) => GroupGradesPageViewFactory


      case GroupScoresPageState(_) => GroupScoresPageViewFactory
      //case AppPageState => AppPageViewFactory
      case _ => ErrorPageViewFactory
    }
    println(s"Finding view factory for $state -> $res")
    res
  }
}
