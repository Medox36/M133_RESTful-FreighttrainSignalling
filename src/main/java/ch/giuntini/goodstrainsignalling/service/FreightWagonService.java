package ch.giuntini.goodstrainsignalling.service;

import ch.giuntini.goodstrainsignalling.data.DataHandler;
import ch.giuntini.goodstrainsignalling.model.FreightWagon;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * freight wagon service
 */
@Path("freightwagon")
public class FreightWagonService {

    /**
     * service to get all freight wagons
     *
     * freight wagons can fe filtered and sorted by a attribute of the class in ascending and descending order
     *
     * @param filter for the wagon Number
     * @param sortBy a attribute of the class FreightWagon
     * @param sort the parameter sortBy with"a" for ascending or "d" for descending
     * @return list of freight wagons
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @QueryParam("contains") String filter,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sort") String sort
    ) {
        List<FreightWagon> freightWagons = DataHandler.readAllFreightWagons();
        List<FreightWagon> copy = new ArrayList<>(freightWagons);

        if (filter != null && !filter.isEmpty()) {
            for (int i = 0; i < copy.size(); i++) {
                copy.removeIf(
                        freightWagon -> !freightWagon.getWaggonNumber().contains(filter)
                );
            }
        }
        if (sortBy == null) {
            sortBy = "waggonNumber";
        }
        if (sort == null) {
            sort = "a";
        }
        if (sort.isEmpty() || (!sort.equals("a") && !sort.equals("d"))) {
            return Response
                    .status(400)
                    .build();
        }
        if (sortBy.matches("waggonNumber") || sortBy.isEmpty()) {
            if (sort.equals("a")) {
                copy.sort(Comparator.comparing(FreightWagon::getWaggonNumber));
            } else {
                copy.sort(Collections.reverseOrder(Comparator.comparing(FreightWagon::getWaggonNumber)));
            }
        } else if (sortBy.matches("lastMainenance")) {
            if (sort.equals("a")) {
                copy.sort(Comparator.comparing(FreightWagon::getLastMaintenance));
            } else {
                copy.sort(Collections.reverseOrder(Comparator.comparing(FreightWagon::getLastMaintenance)));
            }
        } else {
            return Response
                    .status(400)
                    .build();
        }

        return Response
                .status(200)
                .entity(copy)
                .build();
    }

    /**
     * service to get a specific freight wagon by its operation number
     *
     * @param wagonNumber of the freight wagon
     * @return a freight wagon that matches the parameter if there is one
     */
    @GET
    @Path("read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(
            @QueryParam("wn")
            @Pattern(regexp = "([0-9]{2} ){2}[0-9]{4} [0-9]{3}-[0-9]{1}")
            String wagonNumber
    ) {
        FreightWagon freightWagon = DataHandler.readFreightWagonByWaggonNumber(wagonNumber);
        if (freightWagon == null) {
            return Response
                    .status(404)
                    .build();
        }
        return Response
                .status(200)
                .entity(freightWagon)
                .build();
    }

    @POST
    @Path("create")
    @Produces(MediaType.TEXT_PLAIN)
    public Response create(@Valid @BeanParam FreightWagon freightWagon) {
        int status = 200;
        if (!DataHandler.insertFreightWagon(freightWagon)) {
            status = 400;
        }
        return Response
                .status(status)
                .entity("")
                .build();
    }

    @PUT
    @Path("update")
    @Produces(MediaType.TEXT_PLAIN)
    public Response update(@Valid @BeanParam FreightWagon freightWagon) {
        int status = 200;

        FreightWagon oldFreightWagon = DataHandler.readFreightWagonByWaggonNumber(freightWagon.getWaggonNumber());
        if (oldFreightWagon != null) {
            oldFreightWagon.setLastMaintenance(freightWagon.getLastMaintenance());
            oldFreightWagon.setHandbrakeIsOn(freightWagon.getHandbrakeIsOn());

            DataHandler.updateFreightWagon();
        } else {
            status = 410;
        }

        return Response
                .status(status)
                .entity("")
                .build();
    }

    @DELETE
    @Path("delete")
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete(
            @QueryParam("waggonNumber")
            @Pattern(regexp = "([0-9]{2} ){2}[0-9]{4} [0-9]{3}-[0-9]{1}")
            @NotBlank
            String waggonNumber
    ) {
        int status = 200;
        if (!DataHandler.deleteFreightWagon(waggonNumber)) {
            status = 400;
        }

        return Response
                .status(status)
                .entity("")
                .build();
    }
}