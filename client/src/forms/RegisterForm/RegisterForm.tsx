import { useFormik } from "formik"
import UserForm from "../../components/views/UserForm/UserForm"
import GridItem from "../../components/UI/Grid/GridItem/GridItem"
import Button from "../../components/UI/Button/Button"
import FormItem from "../../components/UI/FormItem/FormItem"
import Input from "../../components/UI/Input/Input"
import { useNavigate } from "react-router"
import { AppRoutes } from "../../types/const"
import { register } from "../../services/Endpoints"
import "./RegisterForm.pcss"
import { useContext } from "react"
import { UserContext } from "../../models/UserContext"

interface IRegisterForm {
	submitButtonLabel: string
	onClose: () => void
}

function RegisterForm(
	{
		submitButtonLabel,
		onClose
	}: IRegisterForm
) {
	const context = useContext(UserContext)
	const navigate = useNavigate()

	async function handleSubmit(creds: UserCredentials) {
		await register(creds)
			.then(function (response) {
				let userId = response.data.userId

				context.setUserId(userId)
				navigate(AppRoutes.warning, { replace: true })
			})
			.catch(function (error) {
				console.log(error)
			})
		}
		// try {
		// 	let response = await register(creds)
		// 	console.log(response);

		// 	navigate(AppRoutes.warning, { replace: true })
		// } catch (error) {
		// 	console.log(error)
		// }
	// }

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
		<UserForm formik={formik} classNameGrid="register-form__grid">
			<GridItem className="register-form__grid__close-button-item">
				<Button
					className="register-form__close-button"
					classNameBody="register-form__close-button__body"
					classNameIcon="register-form__close-button__icon"
					icon="close-button"
					onClick={onClose}
				/>
			</GridItem>
			<GridItem className="register-form__grid__email-item">
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
			<GridItem className="register-form__grid__password-item">
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
			<GridItem className="register-form__grid__register-button-item">
				<Button
					className="register-form__register-button"
					classNameBody="register-form__register-button__body"
					type="submit"
				>
					{submitButtonLabel}
				</Button>
			</GridItem>
		</UserForm>
	)
}

export default RegisterForm
