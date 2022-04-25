import { Navigate } from "react-router-dom";
import { logoutUser } from "../../utils/Storage/Storage";

export default function Logout(props) {
    logoutUser();
    props.setUser(null);
    return (
        <Navigate to="/login" />
    );
}
