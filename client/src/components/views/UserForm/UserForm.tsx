import { FormikProps } from "formik"
import Grid from "../../UI/Grid/Grid"
import classNames from "classnames"
import "./UserForm.pcss"

interface IUserForm<Values> {
	className?: string
	children: React.ReactNode
	formik: FormikProps<Values>
	classNameGrid?: string
}

function UserForm<Values>(
	{
		className,
		children,
		formik,
		classNameGrid
	}: IUserForm<Values>
) {
	return (
		<form className={classNames(className, "user-form")} onSubmit={formik.handleSubmit}>
			<Grid className={classNameGrid}>
				{children}
			</Grid>
		</form>
	)
}

export default UserForm
