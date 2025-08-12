package com.arquitectura.venue.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.VenueEvent;
import com.arquitectura.venue.entity.Venue;

public interface VenueEventAdapter extends EventAdapter<Venue, VenueEvent> {
}
