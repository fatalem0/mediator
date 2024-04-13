import { useFormik } from "formik"
import { login } from "../../../services/Endpoints"
import Grid from "../../UI/Grid/Grid"
import GridItem from "../../UI/Grid/GridItem/GridItem"
import FormItem from "../../UI/FormItem/FormItem"
import Input from "../../UI/Input/Input"
import Button from "../../UI/Button/Button"
import classNames from "classnames"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../types/const"
import "./UserForm.pcss"

interface IUserForm {
  className?: string
  classNameCloseButton?: string
  classNameCloseButtonBody?: string
  submitButtonLabel: string
  notice: React.ReactNode
  onClose: () => void
}

function UserForm(
  {
	className,
	classNameCloseButton,
	classNameCloseButtonBody,
	submitButtonLabel,
	notice = null,
	onClose
  }: IUserForm
) {
  const navigate = useNavigate()

  async function handleSubmit(loginData: LoginData) {
	try {
	  let response = await login(loginData)
	  console.log(response);

	  navigate(AppRoutes.warning, { replace: true })
	} catch (error) {
	  console.log(error)
	}
  }

  const formik = useFormik({
	initialValues: {
	  email: '',
	  password: ''
	},
	onSubmit: async values => {
	  const loginData: LoginData = {
		email: values.email,
		password: values.password
	  }

	   await handleSubmit(loginData)
	}
  })

  return (
    <form className={classNames(className, "user-form")} onSubmit={formik.handleSubmit}>
	  <Grid>
	    <GridItem>
		  <Button
		    className={classNames(classNameCloseButton, "user-form__close-button")}
			classNameBody={classNames(classNameCloseButtonBody, "user-form__close_button__body")}
			classNameIcon="user-form__icon"
			icon="close-button"
			onClick={() => onClose()}
		  />
		</GridItem>
		<GridItem>
		  <FormItem>
			<Input
			  id="email"
			  name="email"
			  type="email"
			  value={formik.values.email}
			  label="Почта"
			  onChange={formik.handleChange}
			/>
		  </FormItem>
		</GridItem>
		<GridItem>
		  <FormItem>
		    <Input
			  id="password"
			  name="password"
			  type="password"
			  value={formik.values.password}
			  label="Пароль"
			  onChange={formik.handleChange}
		    />
		  </FormItem>
		</GridItem>
		<GridItem>
		  <Button
		    className="user-form__submit-button"
			classNameBody="user-form__submit-button__body"
			type="submit"
		  >
			{submitButtonLabel}
		  </Button>
		  {Boolean(notice) && <div className="user-form__notice">{notice}</div>}
		</GridItem>
	  </Grid>
	</form>
  )
}

export default UserForm
