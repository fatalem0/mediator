import { useContext, useEffect, useState } from "react"
import { UserContext } from "../../../models/UserContext"
import { Unstable_Grid2 as Grid } from "@mui/material"
import { PotentialFriendCard } from "../../../widgets/potential-friend-card"
import './feedPage.pcss'
import { Box } from "@mui/material"
import { Header } from "../../../widgets/app-header"
import { useInfiniteQuery } from "@tanstack/react-query"
import { IUserPotentialFriend, getUserPotentialFriends } from "../../../entities/user"
import InfiniteScroll from "react-infinite-scroll-component"

export const FeedPage = () => {
	const context = useContext(UserContext)
	// const [userPotentialFriends, setUserPotentialFriends] = useState<IUserPotentialFriend[]>([])

	const { data, fetchNextPage, hasNextPage } = useInfiniteQuery({
		queryKey: ['artists'],
		queryFn: async ({ pageParam }) => getUserPotentialFriends(context.userId, pageParam),
		initialPageParam: 0,
		getNextPageParam: (lastPage) => {
			const nextOffset = lastPage.prevOffset + 4

			return nextOffset > 30 ? undefined : nextOffset
		},
	})

// 	useEffect(() => {
// 		if (data?.pages) {
// 				const existingIds = new Set(userPotentialFriends.map(potentialFriend => potentialFriend.id));
// 				console.log(existingIds)
// 				const newPotentialFriends = data.pages.flatMap(page =>
// 						page.potentialFriends?.filter(potentialFriend => !existingIds.has(potentialFriend.id))
// 							// .map(potentialFriend => ({
// 							// 	id: potentialFriend.id,
// 							// 	name: artist.name,
// 							// 	genreName: artist.genreName,
// 							// 	imageUrl: artist.imageUrl,
// 							// 	isSelected: !!favoriteArtistButtons.find(a => a.id === artist.id)?.isSelected
// 							// }))
// 				);
// 				console.log(newPotentialFriends)
// 				setUserPotentialFriends(prev => [...prev, ...newPotentialFriends]);
// 		}
// }, [data])

	// useEffect(() => {
		// const renderPotentialFriends = async () => {
		// 	const response = await getUserPotentialFriends(context.userId)
		// 	console.log(response)

		// 	setUserPotentialFriendCards(response)
		// }

		// renderMatches()
		// const response = [
		// 	{
		// 		id: '8e183b55-f567-49ce-8ef4-99e5d7f1b520',
		// 		name: 'Артем',
		// 		imageUrl: 'https://i.discogs.com/BS3W-WDYXuQMwoS8pwYBAvXIxT1mNKeLeg7W0A9l5sk/rs:fit/g:sm/q:90/h:450/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTEyNTI0/Ni0xNTI5MTk4Mjg4/LTY0MzAuanBlZw.jpeg',
		// 		about: 'Я артем! всем привет',
		// 		favoriteArtistNames: ['Nirvana', 'The Beatles'],
		// 		favoriteGenreNames: ['Рок', 'Поп', 'Метал'],
		// 		userPurposeNames: ['Создать группу'],
		// 		matchPercent: 99.99,
		// 		city: 'Москва'
		// 	},
		// 	{
		// 		id: '8e183b55-f567-49ce-8ef4-99e5d7f1b520',
		// 		name: 'Артем',
		// 		imageUrl: 'https://i.discogs.com/BS3W-WDYXuQMwoS8pwYBAvXIxT1mNKeLeg7W0A9l5sk/rs:fit/g:sm/q:90/h:450/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTEyNTI0/Ni0xNTI5MTk4Mjg4/LTY0MzAuanBlZw.jpeg',
		// 		about: 'Я артем! всем привет',
		// 		favoriteArtistNames: ['Nirvana', 'The Beatles'],
		// 		favoriteGenreNames: ['Рок', 'Поп', 'Метал'],
		// 		userPurposeNames: ['Создать группу'],
		// 		matchPercent: 99.99,
		// 		city: 'Москва'
		// 	},
		// 	{
		// 		id: '8e183b55-f567-49ce-8ef4-99e5d7f1b520',
		// 		name: 'Артем',
		// 		imageUrl: 'https://i.discogs.com/BS3W-WDYXuQMwoS8pwYBAvXIxT1mNKeLeg7W0A9l5sk/rs:fit/g:sm/q:90/h:450/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTEyNTI0/Ni0xNTI5MTk4Mjg4/LTY0MzAuanBlZw.jpeg',
		// 		about: 'Я артем! всем привет',
		// 		favoriteArtistNames: ['Nirvana', 'The Beatles'],
		// 		favoriteGenreNames: ['Рок', 'Поп', 'Метал'],
		// 		userPurposeNames: ['Создать группу'],
		// 		matchPercent: 99.99,
		// 		city: 'Москва'
		// 	},
		// 	{
		// 		id: '8e183b55-f567-49ce-8ef4-99e5d7f1b520',
		// 		name: 'Артем',
		// 		imageUrl: 'https://i.discogs.com/BS3W-WDYXuQMwoS8pwYBAvXIxT1mNKeLeg7W0A9l5sk/rs:fit/g:sm/q:90/h:450/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTEyNTI0/Ni0xNTI5MTk4Mjg4/LTY0MzAuanBlZw.jpeg',
		// 		about: 'Я артем! всем привет',
		// 		favoriteArtistNames: ['Nirvana', 'The Beatles'],
		// 		favoriteGenreNames: ['Рок', 'Поп', 'Метал'],
		// 		userPurposeNames: ['Создать группу'],
		// 		matchPercent: 99.99,
		// 		city: 'Москва'
		// 	},
		// ] as IUserPotentialFriend[]

		// setUserPotentialFriends(response)
	// }, [])

	const userPotentialFriends = data?.pages.map(page => page.potentialFriends).reduce((acc, potentialFriend) => {
		return [...acc, ...potentialFriend]
	}, [])

	return (
		<div className='feed-page'>
			<Header></Header>
			<Box></Box>
			{/* <Grid container spacing={3} columns={2} direction="row" sx={{ justifyContent: 'center' }}> */}
			<InfiniteScroll
				dataLength={userPotentialFriends ? userPotentialFriends.length : 0}
				next={() => fetchNextPage()}
				hasMore={hasNextPage}
				loader={<div>Loading...</div>}
			>
				<Grid container spacing={3} columns={2} direction="row" sx={{ justifyContent: 'center' }}>
					{userPotentialFriends?.map((potentialFriend, index) => (
						<Grid>
							<PotentialFriendCard
								id={potentialFriend.id}
								imageURL={potentialFriend?.imageURL}
								accountName={potentialFriend.accountName}
								city={potentialFriend.city}
								about={potentialFriend.about}
								userPurposes={potentialFriend.userPurposes}
								favoriteGenres={potentialFriend.favoriteGenres}
								favoriteArtists={potentialFriend.favoriteArtists}
								matchingPercent={potentialFriend.matchingPercent}
								// onClick={() => handleOnClick(index)}
							/>
						</Grid>
					))}
				</Grid>
			</InfiniteScroll>
				{/* <Grid>
					<PotentialFriendCard>

					</PotentialFriendCard>
				</Grid>
				<Grid>
					<PotentialFriendCard>

					</PotentialFriendCard>
				</Grid>
			</Grid> */}
		</div>
	)
}
