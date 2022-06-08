package ch.giuntini.goodstrainsignalling.service;

import ch.giuntini.goodstrainsignalling.data.DataHandler;
import ch.giuntini.goodstrainsignalling.model.Locomotive;

import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * locomotive service
 */
@Path("locomotive")
public class LocomotiveService {

    /**
     * service to get all locomotives
     *
     * locomotives can be filtered an sorted by a attribute of the class in ascending and descending order
     *
     * @param filter for the series
     * @param sortBy a attribute of the class Locomotive
     * @param sort the parameter sortBy with "a" for ascending or "d" for descending
     * @return list of locomotives
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @QueryParam("contains")
            String filter,

            @QueryParam("sortBy")
            @DefaultValue("series")
            String sortBy,

            @QueryParam("sort")
            @DefaultValue("a")
            String sort
    ) {
        if ((sort.isEmpty() || (!sort.equals("a") && !sort.equals("d")))
                || ((!sortBy.matches("series") && !sortBy.matches("operationNumber")
                    && !sortBy.matches("railwayCompany") && !sortBy.matches("commissioningDate")))
        ) {
            return Response
                    .status(400)
                    .build();
        }
        List<Locomotive> list = DataHandler.readAllLocomotivesWithFilterAndSort(filter, sortBy, sort);

        return Response
                .status(200)
                .entity(list)
                .build();
    }

    /**
     * service to get a specific locomotive by its series and operation number
     *
     * @param series of the locomotive
     * @param operationNumber of the locomotive
     * @return a locomotive that matches the parameters if there is one
     */
    @GET
    @Path("read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(
            @QueryParam("series")
            @Pattern(regexp = "([A-Za-z]{1,5}) (([1-9]+[x]?[1-9]*/[1-9]+)|([0-9]{1,3})|(TEE))([\\^]?)([IV]{0,3})")
            String series,

            @QueryParam("opn")
            @Min(101)
            @Max(18841)
            Integer operationNumber
    ) {
        Locomotive locomotive = DataHandler.readLocomotiveBySeriesAndProductionNumber(series, operationNumber);
        if (locomotive == null) {
            return Response
                    .status(404)
                    .build();
        }
        return Response
                .status(200)
                .entity(locomotive)
                .build();
    }

    /**
     * inserts a new locomotive
     *
     * @param locomotive to be inserted
     * @param signalBoxTrackSection the track section of the signal box
     * @return Response
     */
    @POST
    @Path("create")
    @Produces(MediaType.TEXT_PLAIN)
    public Response create(
            @Valid
            @BeanParam
            Locomotive locomotive,

            @FormParam("signalBoxTrackSection")
            @Size(min = 3, max = 120)
            @NotBlank
            String signalBoxTrackSection
    ) {
        int status = 200;

        locomotive.setSignalBox(signalBoxTrackSection);
        if (!DataHandler.insertLocomotive(locomotive)) {
            status = 400;
        }

        return Response
                .status(status)
                .entity("")
                .build();
    }

    /**
     * updates a locomotive
     *
     * @param locomotive the updated locomotive
     * @param signalBoxTrackSection the track section of the new/updated signal box
     * @return Response
     */
    @PUT
    @Path("update")
    @Produces(MediaType.TEXT_PLAIN)
    public Response update(
            @Valid
            @BeanParam
            Locomotive locomotive,

            @FormParam("signalBoxTrackSection")
            @Size(min = 3, max = 120)
            @NotBlank
            String signalBoxTrackSection
    ) {
        int status = 200;
        Locomotive oldLocomotive = DataHandler
                .readLocomotiveBySeriesAndProductionNumber(locomotive.getSeries(), locomotive.getOperationNumber());
        if (oldLocomotive != null) {
            oldLocomotive.setRailwayCompany(locomotive.getRailwayCompany());
            oldLocomotive.setCommissioningDate(locomotive.getCommissioningDate());
            oldLocomotive.setSignalBox(signalBoxTrackSection);

            DataHandler.updateLocomotive();
        } else {
            status = 410;
        }

        return Response
                .status(status)
                .entity("")
                .build();
    }

    /**
     * deletes a locomotive identified by its series and operation number
     *
     * @param series of the locomotive
     * @param operationNumber of the locomotive
     * @return Response
     */
    @DELETE
    @Path("delete")
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete(
            @QueryParam("series")
            @Pattern(regexp = "([A-Za-z]{1,5}) (([1-9]+[x]?[1-9]*/[1-9]+)|([0-9]{1,3})|(TEE))([\\^]?)([IV]{0,3})")
            @NotBlank
            String series,

            @QueryParam("operationNumber")
            @Min(101)
            @Max(18841)
            @NotBlank
            Integer operationNumber
    ) {
        int status = 200;
        if (!DataHandler.deleteLocomotive(series, operationNumber)) {
            status = 400;
        }

        return Response
                .status(status)
                .entity("")
                .build();
    }
}