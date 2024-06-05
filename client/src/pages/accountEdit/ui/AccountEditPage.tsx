import { useContext, useEffect, useState } from "react"
import { UserContext } from "../../../models/UserContext"
import { Header } from "../../../widgets/app-header"
import { Avatar, Button, FormControl, FormHelperText, IconButton, Input, InputLabel, Stack, TextField, Typography } from "@mui/material"
import Autocomplete, { AutocompleteRenderInputParams } from '@mui/material/Autocomplete';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import './accountEditPage.pcss'
import { Field, Form, Formik, useFormik } from "formik";
import * as Yup from 'yup';
import { ICity, getCities } from "../../../shared/api/city";
import { IUserPurpose, getUserPurposes } from "../../../shared/api/user-purpose";

export const AccountEditPage = () => {
	const context = useContext(UserContext)
	const [cityVariants, setCityVariants] = useState<ICity[]>([])
	const [userPurposeVariants, setUserPurposeVariants] = useState<IUserPurpose[]>([])

	useEffect(() => {
		const renderCityVariants = async () => {
			const response = await getCities()
			console.log(response)

			setCityVariants(response)
		}

		const renderUserPurposeVariants = async () => {
			const response = await getUserPurposes()
			console.log(response)

			setUserPurposeVariants(response)
		}

		renderCityVariants()
		renderUserPurposeVariants()
	}, [])

	// const cities = [
	// 	'Москва',
	// 	'Екатеринбург',
	// 	'Тюмень',
	// 	'Вологда',
	// 	'Санкт-Петербург',
	// 	'Антоновка'
	// ] as string[]

	// const userPurposes = [
	// 	'Обмен музыкой',
	// 	'Собрать группу',
	// 	'Пойти на концерт',
	// 	'Создавать музыку',
	// 	'Обучение музыке'
	// ] as string[]

	const AccountEditSchema = Yup.object().shape({
		email: Yup.string().email('Неправильный email').required('Введите email'),
		city: Yup.string().required('Укажите город'),
		userPurpose: Yup.array().required('Выберите минимум одно значение'),
		about: Yup.string().max(499, 'Лимит символов').required('Напишите о себе')
	})

	return (
		<div className='account-edit-page'>
			<Header></Header>
			<div className='account-edit-page__body'>
				<div className='account-edit-page__back-button'>
					<IconButton aria-label='back'>
						<ArrowBackIcon className="account-edit-page__back-button__icon"></ArrowBackIcon>
					</IconButton>
				</div>
				<div className='account-edit-page__edit-account'>
					<Typography variant="h3" >
						Профиль
					</Typography>
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
							email: '',
							city: '',
							userPurpose: [userPurposeVariants[0]],
							about: ''
						}}
						validationSchema={AccountEditSchema}
						onSubmit={values => {
							console.log(values)
						}}
					>
						{({ errors, isValid, touched, dirty }) => (
							<Form>
								<Stack spacing={4}>
									<Field
										fullWidth
										as={TextField}
										name="email"
										type="email"
										variant="outlined"
										label="Почта"
										error={Boolean(errors.email) && Boolean(touched.email)}
										helperText={Boolean(touched.email) && errors.email}
									/>
									<Field
										fullWidth
										as={Autocomplete}
										options={cityVariants}
										defaultValue={[cityVariants[0]]}
										name="city"
										renderInput={(params) => (
											<TextField
												{...params}
												variant="outlined"
												label="Город"
												error={Boolean(errors.city) && Boolean(touched.city)}
												helperText={Boolean(touched.city) && errors.city}
											/>
										)}
									/>
									<Field
										fullWidth
										multiple
										filterSelectedOptions
										as={Autocomplete}
										options={userPurposeVariants}
										defaultValue={[userPurposeVariants[0]]}
										name="userPurpose"
										renderInput={(params) => (
											<TextField
												{...params}
												variant="outlined"
												label="Интересы"
												error={Boolean(errors.userPurpose) && Boolean(touched.userPurpose)}
												helperText={Boolean(touched.userPurpose) && errors.userPurpose}
											/>
										)}
									/>
									<Field
										fullWidth
										multiline
										name="about"
										as={TextField}
										variant="outlined"
										label="О себе"
										inputProps={{ maxLength: 500 }}
										error={Boolean(errors.about) && Boolean(touched.about)}
										helperText={Boolean(touched.about) && errors.about}
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
