/*
 * Copyright (C) 2019 Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
*/
package org.eclipsefoundation.marketplace.resource;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipsefoundation.marketplace.dao.impl.DefaultSolrDao;
import org.eclipsefoundation.marketplace.dao.mapper.ListingMapper;
import org.eclipsefoundation.marketplace.dto.Install;
import org.eclipsefoundation.marketplace.dto.Listing;
import org.eclipsefoundation.marketplace.helper.SolrHelper;
import org.eclipsefoundation.marketplace.model.QueryParams;
import org.eclipsefoundation.marketplace.namespace.UrlParameterNames;
import org.eclipsefoundation.marketplace.service.CachingService;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource for retrieving listings from the Solr instance.
 * 
 * @author Martin Lowe
 */
@Path("/listings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ListingResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(ListingResource.class);

	@Inject
	DefaultSolrDao dao;

	@Inject
	CachingService<List<Listing>> cachingService;

	@Context
	private UriInfo uriInfo;

	/**
	 * Endpoint for /listing/ to retrieve all listings from the database along with
	 * the given query string parameters.
	 * 
	 * @param listingId int version of the listing ID
	 * @return response for the browser
	 */
	@GET
	public Response select() {
		// retrieve the query parameters, and add to a modifiable map
		QueryParams params = new QueryParams(uriInfo.getQueryParameters());
		params.addParam(UrlParameterNames.SOLR_QUERY_STRING, "entity_type:node");
		// retrieve the possible cached object
		Optional<List<Listing>> cachedResults = cachingService.get("all", params,
				() -> dao.get(SolrHelper.createQuery(params), new ListingMapper()));
		if (!cachedResults.isPresent()) {
			LOGGER.error("Error while retrieving cached listings");
			return Response.serverError().build();
		}

		// return the results as a response
		return Response.ok(cachedResults.get()).build();
	}

	/**
	 * Endpoint for /listing/\<listingId\> to retrieve a specific listing from the
	 * database.
	 * 
	 * @param listingId int version of the listing ID
	 * @return response for the browser
	 */
	@GET
	@Path("/{listingId}")
	public Response select(@PathParam("listingId") int listingId) {

		// retrieve the query parameters, and add to a modifiable map
		QueryParams params = new QueryParams(uriInfo.getQueryParameters());
		params.addParam(UrlParameterNames.SOLR_QUERY_STRING, "entity_type:node");
		params.addParam(UrlParameterNames.SOLR_QUERY_STRING, "entity_id:" + listingId);

		// retrieve a cached version of the value for the current listing
		Optional<List<Listing>> cachedResults = cachingService.get(Integer.toString(listingId), params,
				() -> dao.get(SolrHelper.createQuery(params), new ListingMapper()));
		if (!cachedResults.isPresent()) {
			LOGGER.error("Error while retrieving cached listing for ID {}", listingId);
			return Response.serverError().build();
		}

		// return the results as a response
		return Response.ok(cachedResults.get().get(0)).build();
	}

	/**
	 * Endpoint for /listing/\<listingId\>/installs to retrieve install metrics for
	 * a specific listing from the database.
	 * 
	 * @param listingId int version of the listing ID
	 * @return response for the browser
	 */
	@GET
	@Path("/{listingId}/installs")
	public Response selectInstallMetrics(@PathParam("listingId") int listingId) {
		throw new UnsupportedOperationException("Getting install statistics is not yet supported");
	}

	/**
	 * Endpoint for /listing/\<listingId\>/installs/\<version\> to retrieve install
	 * metrics for a specific listing version from the database.
	 * 
	 * @param listingId int version of the listing ID
	 * @param version   int version of the listing version number
	 * @return response for the browser
	 */
	@GET
	@Path("/{listingId}/versions/{version}/installs")
	public Response selectInstallMetrics(@PathParam("listingId") int listingId, @PathParam("version") int version) {
		throw new UnsupportedOperationException("Getting install statistics is not yet supported");
	}

	/**
	 * Endpoint for /listing/\<listingId\>/installs/\<version\> to post install
	 * metrics for a specific listing version to a database.
	 * 
	 * @param listingId int version of the listing ID
	 * @param version   int version of the listing version number
	 * @return response for the browser
	 */
	@POST
	@Path("/{listingId}/versions/{version}/installs")
	public Response postInstallMetrics(@PathParam("listingId") int listingId, @PathParam("version") int version,
			Install installDetails) {
		return Response.ok(installDetails).build();
	}
}