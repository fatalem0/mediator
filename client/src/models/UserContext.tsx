import { createContext, useState } from "react";

interface IUserContext {
	userId: string
	setUserId: React.Dispatch<React.SetStateAction<string>>
}

interface IUserProvider {
	children: React.ReactNode
}
// TODO: delete default userId value
export const UserContext = createContext<IUserContext>({ userId: '', setUserId: () => {}});

function UserProvider({ children }: IUserProvider) {
	const [ currentUserId, setCurrentUserId ] = useState<string>('faf0700d-87a9-498c-8e09-001e634c0516')

	console.log('userContext', currentUserId)

	return (
		<UserContext.Provider value={{userId: currentUserId, setUserId: setCurrentUserId}}>
			{children}
		</UserContext.Provider>
	)
}

export default UserProvider
