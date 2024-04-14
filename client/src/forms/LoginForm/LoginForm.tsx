import { useFormik } from "formik"
import UserForm from "../../components/views/UserForm/UserForm"
import GridItem from "../../components/UI/Grid/GridItem/GridItem"
import Button from "../../components/UI/Button/Button"
import FormItem from "../../components/UI/FormItem/FormItem"
import Input from "../../components/UI/Input/Input"
import { useNavigate } from "react-router"
import { login } from "../../services/Endpoints"
import { AppRoutes } from "../../types/const"
import "./LoginForm.pcss"

interface ILoginForm {
	submitButtonLabel: string
	registerButtonLabel: string
	handleOnRegisterPopupOpen: () => void
	onClose: () => void
}

function LoginForm(
	{
		submitButtonLabel,
		registerButtonLabel,
		handleOnRegisterPopupOpen,
		onClose
	}: ILoginForm
) {
	const navigate = useNavigate()

	async function handleSubmit(creds: UserCredentials) {
		try {
			let response = await login(creds)
			console.log(response);

			navigate(AppRoutes.stub, { replace: true })
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
			const creds: UserCredentials = {
				email: values.email,
				password: values.password
			}

			await handleSubmit(creds)
		}
	})

	return (
		<UserForm formik={formik} classNameGrid="login-form__grid">
			<GridItem className="login-form__grid__close-button-item">
				<Button
					className="login-form__close-button"
					classNameBody="login-form__close-button__body"
					classNameIcon="login-form__close-button__icon"
					icon="close-button"
					onClick={onClose}
				/>
			</GridItem>
			<GridItem className="login-form__grid__email-item">
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
			<GridItem className="login-form__grid__password-item">
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
			<GridItem className="login-form__grid__login-button-item">
				<Button
					className="login-form__login-button"
					classNameBody="login-form__login-button__body"
					type="submit"
				>
					{submitButtonLabel}
				</Button>
			</GridItem>
			<GridItem className="login-form__grid__register-button-item">
				<Button
					className="login-form__register-button"
					classNameBody="login-form__register-button__body"
					onClick={handleOnRegisterPopupOpen}
				>
					{registerButtonLabel}
				</Button>
			</GridItem>
		</UserForm>
	)
}

export default LoginForm
