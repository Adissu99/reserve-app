package com.adissu.reserve.service;

import com.adissu.reserve.entity.CancelledReservation;
import com.adissu.reserve.entity.Client;
import com.adissu.reserve.entity.Product;
import com.adissu.reserve.entity.Reservation;
import com.adissu.reserve.repository.CancelledReservationRepository;
import com.adissu.reserve.repository.ClientRepository;
import com.adissu.reserve.repository.ProductRepository;
import com.adissu.reserve.repository.ReservationRepository;
import com.adissu.reserve.util.DateUtil;
import com.adissu.reserve.util.MailUtil;
import com.adissu.reserve.util.ReserveUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final ReserveUtil reserveUtil;
    private final MailUtil mailUtil;
    private final CancelledReservationRepository cancelledReservationRepository;

    @Deprecated
    public boolean reserve(final Product product, final Client client, final Date selectedDate, final String selectedTime) {


        return true;
    }

    public boolean reserveTest(final int productId, final int clientId, final String selectedTime) {
        log.info("Got the following parameters: ProductId = {} ; ClientId = {} ; SelectedTime = {}", productId, clientId, selectedTime);
        Product product = productRepository.findById(productId).get();
        Client client = clientRepository.findById(clientId).get();
        Reservation reservation = Reservation.builder()
                .product(product)
                .client(client)
                .selectedDate(new Date())
                .selectedTime(selectedTime)
                .requestedCancellation(false)
                .build();

        reservationRepository.save(reservation);

        return true;
    }

    public List<String> getFreeTimeForDate(final String date, final String productName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date selectedDate;
        try {
            selectedDate = sdf.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        log.info("Received date {} and product name {}", date, productName);
        Optional<List<Reservation>> reservationsOptional = reservationRepository.findAllBySelectedDate(selectedDate);
        List<Product> productList = productRepository.findAll();

        if( reservationsOptional.isEmpty() ) {
            return reserveUtil.getFreeDay();
        }

        List<Reservation> reservationList = reservationsOptional.get();
        HashMap<String, Integer> occupiedHoursWithDuration = new HashMap<>();
        for (Reservation reservation : reservationList) {
            occupiedHoursWithDuration.put(reservation.getSelectedTime(), reservation.getProduct().getDurationInMinutes());
        }

        Set<Integer> availableDurationsInMinutes = new HashSet<>();
        for (Product individualProduct : productList ) {
            availableDurationsInMinutes.add(individualProduct.getDurationInMinutes());
        }

        log.info("ReservationService - getFreeTimeForDate - looping through occupiedHoursWithDuration");
        occupiedHoursWithDuration.forEach((hour, duration) -> log.info("Hour: {}; Duration: {}", hour, duration));
        log.info("ReservationService - getFreeTimeForDate - loop has ended for occupiedHoursWithDuration");

        Product product = productRepository.findByProductName(productName);

        log.info("ReservationService - getFreeTimeForDate - got product with name {} and id {} and durationInMinutes {}", product.getProductName(), product.getId(), product.getDurationInMinutes());
        List<String> availableTime = reserveUtil.getFree(occupiedHoursWithDuration, product.getDurationInMinutes(), availableDurationsInMinutes);

        log.info("Got list of {} elements", availableTime.size());

        return availableTime;
    }

    public String doReserve(final String reserveHour, final String clientName, final String productName, final String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date selectedDate;
        try {
            selectedDate = sdf.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        log.info("Got date {}; from dateString {}", selectedDate.getTime(), dateString);

        Optional<Client> client = clientRepository.getByEmail(clientName);
        if( client.isEmpty() ) {
            log.info("Client {} does not exist.", clientName);
            return "FAIL.CLIENT";
        }

        Product product = productRepository.findByProductName(productName);
        if( product == null ) {
            log.info("Product {} does not exist.", productName);
            return "FAIL.PRODUCT";
        }

        Reservation reservation = Reservation.builder()
                .selectedTime(reserveHour)
                .selectedDate(selectedDate)
                .client(client.get())
                .product(product)
                .requestedCancellation(false)
                .build();

        reservationRepository.save(reservation);

        return "SUCCESS";
    }

    public String cancelReservation(final String reservationId) {
        int reserveId = Integer.parseInt(reservationId);
        Optional<Reservation> reservation = reservationRepository.findById(reserveId);
        if( reservation.isEmpty() ) {
            return "FAIL";
        }

        CancelledReservation cancelledReservation = CancelledReservation.builder()
                .client(reservation.get().getClient())
                .reservationDate(reservation.get().getSelectedDate())
                .reservationHour(reservation.get().getSelectedTime())
                .reservationProduct(reservation.get().getProduct().getProductName())
                .cancelDate(DateUtil.getDateWithoutTimeFromToday(0))
                .done(true)
                .requested(false)
                .build();
        cancelledReservationRepository.save(cancelledReservation);

        reservationRepository.delete(reservation.get());
        log.info("Cancelled reservation.");

        return "SUCCESS";
    }

    public String requestCancelReservation(final String reservationId) {
        int reserveId = Integer.parseInt(reservationId);

        Optional<Reservation> reservation = reservationRepository.findById(reserveId);
        if( reservation.isEmpty() ) {
            return "FAIL";
        }

        // send mail to admins
        mailUtil.sendRequestCancelReservation(reservation.get());

        // mark it as requested
        CancelledReservation cancelledReservation = CancelledReservation.builder()
                .client(reservation.get().getClient())
                .reservationDate(reservation.get().getSelectedDate())
                .reservationHour(reservation.get().getSelectedTime())
                .reservationProduct(reservation.get().getProduct().getProductName())
                .cancelDate(DateUtil.getDateWithoutTimeFromToday(0))
                .done(false)
                .requested(true)
                .build();
        cancelledReservationRepository.save(cancelledReservation);
        reservation.get().setRequestedCancellation(true);
        reservationRepository.save(reservation.get());

        log.info("Saved request for cancellation.");

        return "SUCCESS";
    }
}
