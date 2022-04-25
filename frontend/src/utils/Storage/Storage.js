export const saveToSessionStorage = (key, value) => {
    sessionStorage.setItem(key, value);
}

export const getFromSessionStorage = (key) => {
    return sessionStorage.getItem(key);
}

export const removeFromSessionStorage = (key) => {
    sessionStorage.removeItem(key);
}

export const storeLoggedInUser = (accessToken) => {
    let user = {
        'authdata': accessToken,
    }
    saveToSessionStorage('user', JSON.stringify(user));
}

export const getLoggedInUser  = () => {
    let userData = getFromSessionStorage('user');
    if (userData === undefined) {
        return null;
    }

    return JSON.parse(userData);
}

export const logoutUser = () => {
    removeFromSessionStorage('user');
}

export const getAuthorizationHeader = () => {
    const user = getLoggedInUser();
    return `Bearer ${user.authdata}`;
}