    /**
     * 
     * @module service_application
     * 
     * @description Apply the logic application about the route functions.
     * 
     */

    const ERROR = require("../errors/errors");
    const routeDao = require("../data_access/route_DAO");
    const originDao = require("../data_access/origin_DAO");
    const personDao = require("../data_access/person_DAO");

    function createRoute(route) {

        return checkRequirements(route)
        .then(() => originDao.getOriginById(route.origin))
        .then((origin) => {
            
            if(origin == null) throw ERROR.originDoesntExists;
            else return personDao.getUserById(route.driver);
        })
        .then((driver)=>{

            route.max = Number(route.max);
            route.passengersNumber = 0;

            if(driver == null) throw ERROR.userDoesntExists;
            else if(driver.type != "driver") throw ERROR.noPermissions;
            else return routeDao.insertRoute(route);
        });
    }

    function getRouteById(id, user) {

        let routeFin;

        return routeDao.getRouteById(id)
        .then((route) => {

            if(route == null) throw ERROR.routeDoesntExists;
            else {

                routeFin = route;
                return personDao.getUserById(route.driver);
            }
        })
        .then((driver) => {

            routeFin.driverName = driver.name;
            routeFin.driverSurname = driver.surname;
            routeFin.driverNumber = driver.number;
            routeFin.driverEmail = driver.email;

            return originDao.getOriginById(routeFin.origin);  
        })
        .then((origin) => {

            routeFin.origin = origin.name;

            if(user == null) return routeFin;
            else {

                return personDao.getUser(user.email)
                .then((userFull) => {

                    if(userFull == null) throw ERROR.userDoesntExists;
                    return routeDao.getDestination(routeFin.id,userFull.id);
                })
                .then((destination) => {

                    if(destination == null) throw ERROR.userNotAdded;

                    routeFin.destinationName = destination;

                    return routeFin;
                })
            }
        })
    }

    function searchRoutes(origin, destination) {

        destination = Number(destination);

        return routeDao.getRoutesByOriginAndDestination(origin, destination);
    }

    function addToRoute(user,route,address,coordinates) {

        let passenger;

        return personDao.getUser(user.email)
        .then((result) => {

            if (!result) throw ERROR.userDoesntExists;
            else {

                passenger = result;
                return routeDao.getRouteById(route.id);
            }
        })
        .then((route) => {

            if (!route) throw ERROR.routeDoesntExists;
            else  if (route.passengers.length >= route.max) throw ERROR.routeSoldOut;
            else if (route.passengers.commonOf(passenger.id) != -1) throw ERROR.userAlreadyAdded;
            else return routeDao.addToRoute(passenger.id, route, address, coordinates);
        })
    }

    function removePassengerFromRoute(passenger, route) {

        let fullRoute;

        return routeDao.getRouteById(route.id)
        .then((route) => {

            if(route == null) throw ERROR.routeDoesntExists;
            else {

                fullRoute = route;

                return personDao.getUser(passenger.email);
            }
        })
        .then((user) => {

            if(user == null) throw ERROR.userDoesntExists;
            else if(!fullRoute.passengers.some(pass => pass == user.id)) throw ERROR.userNotAdded; 
            return routeDao.removePassengerFromRoute(user.id,fullRoute.id);
        })
    }

    function removeRoute(driver,route) {

        let driverId;

        return personDao.getUser(driver.email)
        .then((driver) => {

            if(driver == null) throw ERROR.userDoesntExists;
            else {

                driverId = driver.id;

                return routeDao.getRouteById(route.id);
            }
        })
        .then((routeFull) => {

            if (routeFull == null) throw ERROR.routeDoesntExists;
            else if(routeFull.driver != driverId) throw ERROR.noPermissions;
            else if (routeFull.passengersNumber > 0) throw ERROR.routeNotEmpty;
            else return routeDao.deleteRouteById(routeFull.id);
        })
    }

    function getRoutesByUser(user) {

        return personDao.getUser(user.email)
        .then((userFull) => {

            if(userFull == null) throw ERROR.userDoesntExists;
            else if(userFull.type == "driver") return routeDao.getRoutesByDriver(userFull.id);
            else if(userFull.type == "passenger") return routeDao.getRoutesByPassenger(userFull.id);
            else throw ERROR.noPermissions;
        });
    }

    function getRoutePoints(route,driver) {

        let driverFull;
        let routeFull;
        let waypoints;

        return personDao.getUser(driver.email)
        .then((data) => {

            if(data == null) throw ERROR.userDoesntExists;

            driverFull = data;

            return routeDao.getRouteById(route.id);
        })
        .then((data) => {

            if(data == null) throw ERROR.routeDoesntExists;

            routeFull = data;

            if(driverFull.id == routeFull.id) throw ERROR.noPermissions;
            else return routeDao.getRoutePoints(routeFull.id);

        }).then((data2) => {

            waypoints = data2;

            return originDao.getOriginById(routeFull.origin);

        }).then((origin) => { return {waypoints:waypoints,origin:origin}; });
    }

    /* **************************************************************************************************** */

    function checkRequirements(route) {

        return new Promise((resolve,reject) => {

            if(route == null || route.driver == null || route.origin == null || route.destination == null ||
            route.max == null || Number(route.max) == NaN || Number(route.max)<0) reject(ERROR.badRequestForm);

            else resolve();
        });
    }


    module.exports = {

        createRoute:createRoute,
        getRouteById:getRouteById,
        searchRoutes:searchRoutes,
        addToRoute:addToRoute,
        removePassengerFromRoute:removePassengerFromRoute,
        removeRoute:removeRoute,
        getRoutesByUser:getRoutesByUser,
        getRoutePoints:getRoutePoints
    }