import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import UserProvider from "../../models/UserContext"
import { RouterProvider } from "react-router-dom"

interface IProviders {
	readonly children: React.ReactNode
}

export const Providers = ({ children }: IProviders) => {
	const queryClient = new QueryClient()

	return (
		<QueryClientProvider client={queryClient}>
			<UserProvider>
				{children}
			</UserProvider>
		</QueryClientProvider>
	)
}
