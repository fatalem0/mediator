import { useContext, useEffect, useState } from "react"
import { UserContext } from "../../../../models/UserContext"
import { IGenre, getGenres } from "../../../../shared/api/genre"
import { ApiError } from "../../../../shared/api/types"
import Button from "@mui/material/Button"
import { Unstable_Grid2 as Grid, IconButton } from "@mui/material"
import { Typography } from "@mui/material"
import { GenreCard } from "../../../../widgets/genre-сard"
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import './chooseFavoriteGenresPage.pcss'
import { IFavoriteGenreIds, updateUserFavoriteGenres } from "../../../../entities/user"

interface IChooseFavoriteGenresPage {
	handleOnBackButtonClick: () => void
	handleOnChooseUserFavoriteArtistsOpen: () => void
}

export function ChooseFavoriteGenresPage({ handleOnBackButtonClick, handleOnChooseUserFavoriteArtistsOpen }: IChooseFavoriteGenresPage) {
	const context = useContext(UserContext)
	const [favoriteUserGenreButtons, setFavoriteUserGenreButtons] = useState<IFavoriteUserGenreButton[]>([])

	interface IFavoriteUserGenreButton {
		id: string
		name: string
		imageUrl: string
		isSelected: boolean
	}

	useEffect(() => {
		const renderButtons = async () => {
			const response = await getGenres()
			console.log(response)

			const newGenres = response.map((genre: IGenre): IFavoriteUserGenreButton => ({
				id: genre.id,
				name: genre.name,
				imageUrl: genre.imageUrl,
				isSelected: false
			}))

			setFavoriteUserGenreButtons(newGenres)
		}

		renderButtons()
	}, [])

	function handleOnClick(index: number): void {
		const updateFavoriteUserGenreButtons = favoriteUserGenreButtons.map((favoriteUserGenre, idx) => {
      if (idx === index) {
        return { ...favoriteUserGenre, isSelected: !favoriteUserGenre.isSelected }
      }
      return favoriteUserGenre
    })

    setFavoriteUserGenreButtons(updateFavoriteUserGenreButtons)
	}

	const handleOnContinueClick = async () => {
		const favoriteGenreIds: IFavoriteGenreIds = {
			favoriteGenreIds: favoriteUserGenreButtons
				.filter((favoriteUserGenreButton: IFavoriteUserGenreButton) => favoriteUserGenreButton.isSelected)
				.map((favoriteUserGenreButton: IFavoriteUserGenreButton) => favoriteUserGenreButton.id)
		}

		await updateUserFavoriteGenres(
			context.userId,
			favoriteGenreIds
		)
			.then(function () {
				console.log(`Favorite genres for user with id = ${context.userId} has been successfully updated`)
			})
			.catch(function (error: ApiError) {
				console.log(error)
			})

		handleOnChooseUserFavoriteArtistsOpen()
	}

	const selectedCount = favoriteUserGenreButtons?.filter(genre => genre.isSelected).length

	return (
		<div className='choose-favorite-genres-page'>
			<div className="choose-favorite-genres-page__body">
				<div className='choose-favorite-genres-page__back-button'>
					<IconButton aria-label='back' onClick={handleOnBackButtonClick}>
						<ArrowBackIcon className="choose-favorite-genres-page__back-button__icon"></ArrowBackIcon>
					</IconButton>
				</div>
				<Typography variant="h2" gutterBottom sx={{ color: "black", alignSelf: "flex-start" }}>
					Какой жанр музыки вам нравится?
				</Typography>
				<Grid container spacing={3} columns={3} sx={{ justifyContent: 'center' }}>
						{favoriteUserGenreButtons?.map((favoriteUserGenreButton, index) => (
							<Grid>
								<GenreCard
									genreName={favoriteUserGenreButton.name}
									imageUrl={favoriteUserGenreButton.imageUrl}
									isSelected={favoriteUserGenreButton.isSelected}
									onClick={() => handleOnClick(index)}
								/>
								{/* <Button
									variant={favoriteUserGenreButton.isSelected ? "contained" : "outlined"}
									onClick={() => handleOnClick(index)}
								>
									{favoriteUserGenreButton.name}
								</Button> */}
							</Grid>
						))}
				</Grid>
				<Button
					variant="contained"
					onClick={handleOnContinueClick}
					disabled={selectedCount < 1}
					sx={{ width: '400px', height: '50px', my: 4, backgroundColor: '#0050FF' }}
				>
					Далее
				</Button>
			</div>
		</div>
	)
}
