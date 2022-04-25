import { getAuthorizationHeader } from "../Storage/Storage";
import moment from "moment";


export const _apiUrl = () => {
    return process.env.MYWEATHER_API_URL ?? 'http://localhost:8080';
}

export const API_URL = _apiUrl();

export const getTemperatureAir = (location, start, stop) => {
    const url = `${API_URL}/temperature/air?location=${location}&` +
        `start=${moment(start).format("YYYY-MM-DD")}T00:00:00&` +
        `stop=${moment(stop).format("YYYY-MM-DD")}T23:59:59`;
    return fetch(url, {
        headers: {
            'Authorization': getAuthorizationHeader(),
        }
    });
}

export const postTemperatureAir = (measurement) => {
    const url = `${API_URL}/temperature/air`;
    return fetch(url, {
        method: 'POST',
        headers: {
            'Authorization': getAuthorizationHeader(),
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(measurement),
    });
}

export const putTemperatureAir = (measurement) => {
    const url = `${API_URL}/temperature/air/${measurement.location}/${measurement.timestamp}`;
    return fetch(url, {
        method: 'PUT',
        headers: {
            'Authorization': getAuthorizationHeader(),
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(measurement),
    });
}

export const deleteTemperatureAir = (measurement) => {
    const url = `${API_URL}/temperature/air/${measurement.location}/${measurement.timestamp}`;
    return fetch(url, {
        method: 'DELETE',
        headers: {
            'Authorization': getAuthorizationHeader(),
        }
    });
}