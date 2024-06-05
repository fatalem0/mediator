import { useContext, useEffect, useState } from "react"
import { UserContext } from "../../../../models/UserContext"
import { useInfiniteQuery } from "@tanstack/react-query"
import { Unstable_Grid2 as Grid } from "@mui/material"
import ArtistCard from "../../../../components/UI/ArtistCard/ArtistCard"
import { getArtists } from "../../../../shared/api/artist"
import InfiniteScroll from 'react-infinite-scroll-component'
import Button from "@mui/material/Button"
import { ApiError } from "../../../../shared/api/types"
import { IconButton, Typography } from "@mui/material"
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import "./chooseUserFavoriteArtistsPage.pcss"
import { IFavoriteArtistIds, updateUserFavoriteArtists } from "../../../../entities/user"

interface IChooseUserFavoriteArtistsPage {
	handleOnBackButtonClick: () => void
	handleOnChooseUserCityAndAccountNameOpen: () => void
}

export function ChooseUserFavoriteArtistsPage(
	{
		handleOnBackButtonClick,
		handleOnChooseUserCityAndAccountNameOpen
	}: IChooseUserFavoriteArtistsPage
) {
	const context = useContext(UserContext)
	const [favoriteArtistButtons, setFavoriteArtistButtons] = useState<IFavoriteArtistButton[]>([])

	interface IFavoriteArtistButton {
		id: string
		name: string
		genreName: string
		imageUrl: string
		isSelected: boolean
	}

	const { data, fetchNextPage, hasNextPage } = useInfiniteQuery({
		queryKey: ['artists'],
		queryFn: async ({ pageParam }) => getArtists(pageParam),
		initialPageParam: 0,
		getNextPageParam: (lastPage) => {
			const nextOffset = lastPage.prevOffset + 6

			return nextOffset > 19 ? undefined : nextOffset
		},
	})

	useEffect(() => {
		if (data?.pages) {
				const existingIds = new Set(favoriteArtistButtons.map(artist => artist.id));
				const newArtists = data.pages.flatMap(page =>
						page.artists.filter(artist => !existingIds.has(artist.id)).map(artist => ({
								id: artist.id,
								name: artist.name,
								genreName: artist.genreName,
								imageUrl: artist.imageUrl,
								isSelected: !!favoriteArtistButtons.find(a => a.id === artist.id)?.isSelected
						}))
				);
				setFavoriteArtistButtons(prev => [...prev, ...newArtists]);
		}
}, [data, favoriteArtistButtons])

	// const artists = data?.pages.reduce((acc, page) => {
	// 	const favoriteArtists = page.artists.map((artist): IFavoriteArtistButton => ({
	// 		id: artist.id,
	// 		name: artist.name,
	// 		imageUrl: artist.imageUrl,
	// 		isSelected: false
	// 	}))

	// 	return acc.concat(favoriteArtists)
	// }, [] as IFavoriteArtistButton[])

	function handleOnClick(index: number): void {
		const updateFavoriteArtistButtons = favoriteArtistButtons?.map((favoriteArtist, idx) => {
      if (idx === index) {
        return { ...favoriteArtist, isSelected: !favoriteArtist.isSelected }
      }

      return favoriteArtist
    })

    setFavoriteArtistButtons(updateFavoriteArtistButtons)
	}

	const handleOnContinueClick = async () => {
		const favoriteArtistIds: IFavoriteArtistIds = {
			favoriteArtistIds: favoriteArtistButtons ?
				favoriteArtistButtons
					.filter((favoriteArtistButton: IFavoriteArtistButton) => favoriteArtistButton.isSelected)
					.map((favoriteArtistButton: IFavoriteArtistButton) => favoriteArtistButton.id)
				: [] as string[]
		}

		await updateUserFavoriteArtists(
			context.userId,
			favoriteArtistIds
		)
			.then(function () {
				console.log(`Favorite artists for user with id = ${context.userId} has been successfully updated`)
			})
			.catch(function (error: ApiError) {
				console.log(error)
			})

		handleOnChooseUserCityAndAccountNameOpen()
	}

	const selectedCount = favoriteArtistButtons?.filter(artist => artist.isSelected).length;

	return (
		<div className="choose-user-favorite-artists-page">
			<div className="choose-user-favorite-artists-page__body">
				<div className='choose-user-favorite-artists-page__back-button'>
					<IconButton aria-label='back' onClick={handleOnBackButtonClick}>
						<ArrowBackIcon className="choose-user-favorite-artists-page__back-button__icon"></ArrowBackIcon>
					</IconButton>
				</div>
				<Typography variant="h2" gutterBottom sx={{ color: "black", alignSelf: "flex-start" }}>
					Выберите минимум 3 любимых исполнителя
				</Typography>
				<InfiniteScroll
					dataLength={favoriteArtistButtons ? favoriteArtistButtons.length : 0}
					next={() => fetchNextPage()}
					hasMore={hasNextPage}
					loader={<div>Loading...</div>}
				>
					<Grid container spacing={3}>
						{favoriteArtistButtons?.map((artist, index) => (
							<Grid lg={4}>
								<ArtistCard
									artistName={artist.name}
									genreName={artist.genreName}
									imageUrl={artist.imageUrl}
									onClick={() => handleOnClick(index)}
									isSelected={artist.isSelected}
								/>
							</Grid>
						))}
					</Grid>
				</InfiniteScroll>
				<div className="choose-user-favorite-artists-page__footer">
					<Button
							className="choose-user-favorite-artists-page__footer__continue-button"
							variant="contained"
							color="primary"
							onClick={handleOnContinueClick}
							disabled={selectedCount < 3}
							sx={{
								backgroundColor: "white",
								color: "black",
								border: "1px solid black",

								'&:disabled': {
									color: "rgba(0, 0, 0, 0.26)",
									bgColor: "rgba(0, 0, 0, 0.12)",
								},

								':hover': {
									backgroundColor: "black",
									color: "white"
								}
							}}
					>
							Выбрать
					</Button>
				</div>
			</div>
		</div>
	)
}
