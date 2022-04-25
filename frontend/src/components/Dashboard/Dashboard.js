import { useEffect } from "react";
import { fetchWithToken } from "../../utils/Api/Api";

export default function Dashboard() {

    useEffect(() => {
        fetchWithToken('http://localhost:8080/temperature/air?start=2000-04-18T00:00:00Z&stop=2022-04-19T00:00:00Z&location=home')
            .then(response => {
                console.info(response);
            })
    }, []);

    return (<h2>Dashboard</h2>);
}