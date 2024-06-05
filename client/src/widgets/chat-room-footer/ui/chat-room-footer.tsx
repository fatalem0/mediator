import { Box } from "@mui/material";
import { TextareaAutosize } from '@mui/base';
import { Field, Form, Formik } from "formik";
import "./chat-room-footer.pcss"

interface IChatRoomFooter {
	handleOnSubmit: () => void
}

export function ChatRoomFooter({ handleOnSubmit }: IChatRoomFooter) {
	const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key == 'Enter') {
      handleOnSubmit();
    }
  };

	return (
		<Box component={TextareaAutosize} placeholder="Сообщение" minRows={2} maxRows={14} sx={{ position: "fixed", bottom: 0, border: "1px solid", width: "74%", height: "10%", px: "0.4em" }}>
			<Formik
				initialValues={{
					message: ''
				}}
				onSubmit={values => {
					console.log(values)
				}}
			>
				<Form onSubmit={handleOnSubmit} className="chat-room-footer-form">
					<Field className="chat-room-footer-form__input" align="center" id="message" name="message" />
				</Form>
			</Formik>
		</Box>
	)
}
