import { Autocomplete, Avatar, Button, IconButton, Stack, TextField, Typography } from "@mui/material"
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import "./createUserDataPage.pcss"
import { Field, Form, Formik } from "formik"
import * as Yup from 'yup'
import { useContext, useEffect, useState } from "react"
import { ICity, getCities } from "../../../../shared/api/city"
import { IUserData, updateUser } from "../../../../entities/user"
import { UserContext } from "../../../../models/UserContext"
import { ApiError } from "../../../../shared/api/types"

interface ICreateUserDataPage {
	handleOnBackButtonClick: () => void
}

export function CreateUserDataPage({ handleOnBackButtonClick }: ICreateUserDataPage) {
	const context = useContext(UserContext)
	const [cities, setCities] = useState<ICity[]>([])

	useEffect(() => {
		const getAndSetCities = async () => {
			const response = await getCities()

			console.log(response)
			setCities(response)
		}

		getAndSetCities()
	}, [])

	const CreateUserDataPageSchema = Yup.object().shape({
		accountName: Yup.string().required('Укажите имя аккаунта'),
		city: Yup.string().required('Укажите город'),
		about: Yup.string().max(1499, 'Лимит символов').required('Напишите о себе')
	})

	const handleOnSubmitClick = async (
		accountName: string,
		about: string,
		cityName: string
	) => {
		const cityId = cities.find((city: ICity) => city.name === cityName)?.id

		const userData: IUserData = {
			accountName: accountName,
			about: about,
			cityId: cityId
		}

		await updateUser(
			context.userId,
			userData
		)
			.then(function () {
				console.log(`User with id = ${context.userId} has been successfully updated`)
			})
			.catch(function (error: ApiError) {
				console.log(error)
			})
	}

	return (
		<div className="create-user-data-page">
			<div className="create-user-data-page__body">
				<div className='create-user-data-page__back-button'>
					<IconButton aria-label='back' onClick={handleOnBackButtonClick}>
						<ArrowBackIcon className="create-user-data-page__back-button__icon"></ArrowBackIcon>
					</IconButton>
				</div>
				<Typography variant="h2" gutterBottom sx={{ color: "black" }}>
						Почти все! Осталось заполнить данные ниже
					</Typography>
				<div className='create-user-data-page__edit-account'>
					<Button
						startIcon={
							<Avatar
								src="https://i.discogs.com/BS3W-WDYXuQMwoS8pwYBAvXIxT1mNKeLeg7W0A9l5sk/rs:fit/g:sm/q:90/h:450/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTEyNTI0/Ni0xNTI5MTk4Mjg4/LTY0MzAuanBlZw.jpeg"
								sx={{ width: "200px", height: "200px", m: 3 }}
							/>
						}
						sx={{ margin: '0 auto', display: "flex" }}
					>
					</Button>
					<Formik
						initialValues={{
							accountName: '',
							city: '',
							about: ''
						}}
						validationSchema={CreateUserDataPageSchema}
						onSubmit={async (values) => {
							await handleOnSubmitClick(
								values.accountName,
								values.about,
								values.city
							)
						}}
					>
						{({ errors, isValid, touched, dirty, setFieldValue }) => (
							<Form>
								<Stack spacing={4}>
									<Field
										fullWidth
										as={TextField}
										name="accountName"
										variant="outlined"
										label="Придумайте имя аккаунта"
										error={Boolean(errors.accountName) && Boolean(touched.accountName)}
										helperText={Boolean(touched.accountName) && errors.accountName}
									/>
									<Field
										fullWidth
										multiline
										name="about"
										as={TextField}
										variant="outlined"
										label="Расскажите о себе другим"
										inputProps={{ maxLength: 1500 }}
										error={Boolean(errors.about) && Boolean(touched.about)}
										helperText={Boolean(touched.about) && errors.about}
									/>
									<Autocomplete
										id="city"
										options={cities.map((city: ICity) => city.name)}
										onChange={(e, value) => {
											console.log(value)
											setFieldValue(
												"city",
												value !== null ? value : ''
											);
										}}
										renderInput={(params) => (
											<TextField
												{...params}
												variant="outlined"
												label="Выберите город проживания"
												error={Boolean(errors.city) && Boolean(touched.city)}
												helperText={Boolean(touched.city) && errors.city}
												name="city"
											/>
										)}
									/>
									<Button
										type="submit"
										variant="contained"
										color="primary"
										size="large"
										disabled={!(dirty && isValid)}
										sx={{ position: "relative", right: 0, bottom: 0, float: 'right' }}
									>
										Сохранить
									</Button>
								</Stack>
							</Form>
						)}
					</Formik>
				</div>
			</div>
		</div>
	)
}
