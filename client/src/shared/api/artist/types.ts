interface Artist {
	id: string
	name: string
	genreName: string
	imageUrl: string
}

export interface ArtistsQuery {
	prevOffset: number
	artists: Artist[]
}
