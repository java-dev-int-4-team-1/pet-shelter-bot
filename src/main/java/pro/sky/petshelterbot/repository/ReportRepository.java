package pro.sky.petshelterbot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pro.sky.petshelterbot.entity.Pet;
import pro.sky.petshelterbot.entity.Report;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    /** all the reports by Pet-Id **/
    List<Report> findByPetId(Long PetId);
    /** all unchecked reports **/
    List<Report> findByCheckedIsFalse();
    /** all checked reports for which the advice to improve should be sent
     * TODO think about how to manage or change this note**/
    List<Report> findByCheckedIsTrueAndApprovedIsFalse();

    Page<Report> findAllByPetId(Long petId, Pageable pageable);

    @Query("from Report r where r.pet.shelter.id = :shelterId")
    Page<Report> findAllByShelterId(Long shelterId, Pageable pageable);

    @Query("from Report r where r.checked = null or r.checked = false")
    Page<Report> findAllUncheckedReports(Pageable pageable);

    @Query("select p from Pet p, Report r where p.adoptionDate is not null and p.adoptionDate >= current_timestamp and p = r.pet and p.shelter.id = :shelterId and not exists (select r from Report r where r.sent >= current_date - 1) group by p")
    List<Pet> findOverdueReports(Long shelterId);
}
