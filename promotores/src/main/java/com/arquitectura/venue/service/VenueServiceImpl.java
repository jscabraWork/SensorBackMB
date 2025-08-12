package com.arquitectura.venue.service;

import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.venue.entity.Venue;
import com.arquitectura.venue.entity.VenueRepository;
import org.springframework.stereotype.Service;

@Service
public class VenueServiceImpl extends CommonServiceImpl<Venue, VenueRepository> implements VenueService {
}
