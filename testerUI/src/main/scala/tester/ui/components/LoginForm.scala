package tester.ui.components

import scala.scalajs.js
import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._

import org.scalajs.dom._
import slinky.core.facade.Hooks.useState
import slinky.core.facade.ReactElement
import typings.antDesignIcons.components.AntdIcon
import typings.antDesignIconsSvg._
import typings.antd.antdStrings
import typings.antd.components.{List => AntList, _}
import typings.antd.components.Form.{Form => FormItem}
import typings.antd.libFormFormMod.{FormLayout, useForm}
import typings.antd.libFormHooksUseFormMod.FormInstance
import typings.antd.libGridColMod.ColProps
import typings.rcFieldForm.esInterfaceMod
import typings.rcFieldForm.esInterfaceMod.{AggregationRule, BaseRule, Rule, RuleObject}
import typings.rcFieldForm.esUtilsMessagesMod.defaultValidateMessages
import typings.react.mod.CSSProperties


object LoginForm {
  case class Props(tryLogin: LoginPassword => Unit)
  def apply(tryLogin: LoginPassword => Unit): ReactElement = {
    import slinky.core.KeyAddingStage.build
    build(component.apply(Props(tryLogin)))
  }


  class LoginPassword(val login: String, val password: String) extends js.Object


  val component = FunctionalComponent[Props] { props =>
    val form: FormInstance[LoginPassword] = useForm[LoginPassword]().head
    div(style := js.Dynamic.literal(
      width = "100vw",
      display = "flex",
      `flex-direction` = "column"
    ))(
    Card()
      .title("Войдите")
      .style(CSSProperties()
        .setMinWidth("300px").setMaxWidth("900px")
        .setAlignSelf("center")
        .setMargin(20).setPadding(5))(
        Form[LoginPassword]()
          .form(form)
          .name("loginForm")
          .labelCol(ColProps().setSpan(5))
          .wrapperCol(ColProps().setSpan(12))
          .style(CSSProperties().setMaxWidth("300"))
          //      .layout(FormLayout.horizontal)
          .onFinish(props.tryLogin)(
            FormItem()
              .label("Логин")
              .name("login")
              .rules(
                js.Array[Rule](
                  AggregationRule().setRequired(true).setMessage("Логин не может быть пустым"),
                )
              )(
                Input()
              ),
            FormItem()
              .label("Пароль")
              .name("password")
              .rules(
                js.Array[Rule](
                  AggregationRule().setRequired(true).setMessage("Пароль не может быть пустым"),
                )
              )(
                Input.Password()
              ),
            FormItem()
              .wrapperCol(ColProps().setOffset(5).setSpan(12))
              .name("Submit")(Button().htmlType(antdStrings.submit).`type`(antdStrings.primary)("Войти"))
          ),
        Divider(),
        p("Старые логины и пароли работают!"),
        p("Если вы забыли, или не получили данные для входа, обращайтесь к вашему преподавателю программирования, или на почту nikita.chubakha@gmail.com")
      )
    )
  }

}
