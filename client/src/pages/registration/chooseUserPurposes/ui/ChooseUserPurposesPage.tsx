import { useContext, useEffect, useState } from "react";
import Stack from '@mui/material/Stack';
import Button from "@mui/material/Button";
import { IconButton, Typography } from "@mui/material";
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useNavigate } from "react-router-dom";
import { UserContext } from "../../../../models/UserContext";
import { AppRoutes } from "../../../../app/routers/endpoints";
import { IUserPurpose, getUserPurposes } from "../../../../shared/api/user-purpose";
import { ApiError } from "../../../../shared/api/types";
import "./chooseUserPurposesPage.pcss"
import { IUserPurposeIds, updateUserPurposes } from "../../../../entities/user";

interface IChooseUserPurposesPage {
	handleOnChooseUserFavoriteGenresOpen: () => void
}

export function ChooseUserPurposesPage({ handleOnChooseUserFavoriteGenresOpen }: IChooseUserPurposesPage) {
	const context = useContext(UserContext)
	const navigate = useNavigate()
	const [userPurposeButtons, setUserPurposeButtons] = useState<IUserPurposeButton[]>([])

	interface IUserPurposeButton {
		id: string
		name: string
		isSelected: boolean
	}

	useEffect(() => {
		const renderButtons = async () => {
			const response = await getUserPurposes()
			console.log(response)

			const newUserPurposes = response.map((userPurpose: IUserPurpose): IUserPurposeButton => ({
				id: userPurpose.id,
				name: userPurpose.name,
				isSelected: false
			}))

			setUserPurposeButtons(newUserPurposes)
		}

		renderButtons()
	}, [])

	function handleOnBackButtonClick() {
		return navigate(AppRoutes.main , { replace: true})
	}

	function handleOnClick(index: number): void {
		const updateUserPurposeButtons = userPurposeButtons.map((purpose, idx) => {
      if (idx === index) {
        return { ...purpose, isSelected: !purpose.isSelected }
      }
      return purpose
    })

    setUserPurposeButtons(updateUserPurposeButtons)
	}

	const handleOnContinueClick = async () => {
		const userPurposeIds: IUserPurposeIds = {
			userPurposeIds: userPurposeButtons
				.filter((userPurposeButton: IUserPurposeButton) => userPurposeButton.isSelected)
				.map((userPurposeButton: IUserPurposeButton) => userPurposeButton.id)
		}

		await updateUserPurposes(
			context.userId,
			userPurposeIds
		)
			.then(function () {
				console.log(`Purposes for user with id = ${context.userId} has been successfully updated`)
			})
			.catch(function (error: ApiError) {
				console.log(error)
			})

		handleOnChooseUserFavoriteGenresOpen()
	}

	// function onContinueButtonClick() {
	// async function onContinueButtonClick() {
	// 	const forUpdateUserData: ForUpdateUserData = {
	// 		interests: selectedInterests
	// 	}

	// 	handleOnUserPreferencesOpen()

	// 	await update(context.userId, forUpdateUserData)
	// 		.then(function (response) {
	// 			console.log(response)

	// 			handleOnUserPreferencesOpen()
	// 		})
	// 		.catch(function (error) {
	// 			console.log(error)
	// 		})
	// }

	const selectedCount = userPurposeButtons?.filter(userPurpose => userPurpose.isSelected).length;

	return (
		<div className="choose-user-purposes-page">
			<div className="choose-user-purposes-page__body">
				<div className='choose-user-purposes-page__back-button'>
					<IconButton aria-label='back' onClick={handleOnBackButtonClick}>
						<ArrowBackIcon className="choose-user-purposes-page__back-button__icon"></ArrowBackIcon>
					</IconButton>
				</div>
				<Stack direction="column" alignItems="center" spacing={5}>
					<Typography variant="h2" sx={{ color: "black" }}>
						Что вам сейчас интересно?
					</Typography>
					<Stack direction="row" spacing={3}>
						{userPurposeButtons?.map((userPurposeButton, index) => (
							<Button
								variant="outlined"
								onClick={() => handleOnClick(index)}
								sx={{
									color: userPurposeButton.isSelected ? "white" : "black",
									bgcolor: userPurposeButton.isSelected ? "black" : "transparent",

									borderRadius: "16px",
									border: "2px solid rgba(0, 0, 0, 0.12)",

									':hover': {
										bgcolor: userPurposeButton.isSelected ? "black" : "transparent",
										border: "2px solid black",
            				color: userPurposeButton.isSelected ? "white" : "black",
									}
								}}
							>
								{userPurposeButton.name}
							</Button>
						))}
					</Stack>
					<Button
						variant="contained"
						onClick={handleOnContinueClick}
						disabled={selectedCount < 1}
						sx={{
							color: "white",
							bgcolor: "black",
							borderRadius: "16px",

							':hover': {
								bgcolor: "black",
								color: "white"
							}
						}}
					>
						Далее
					</Button>
				</Stack>
			</div>
		</div>
	)
}
