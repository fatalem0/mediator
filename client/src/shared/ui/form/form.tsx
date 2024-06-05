import { Box } from "@mui/material"
import { FormikProps } from "formik"

interface IForm {
	children: React.ReactNode
	handleSubmit: (event: React.FormEvent<HTMLFormElement>) => void
}

export function Form({ children, handleSubmit }: IForm) {
	return (
		<Box component='form' onSubmit={handleSubmit}>
			{children}
		</Box>
	)
}
