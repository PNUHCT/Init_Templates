package neoguri.springTemplate.domain.member.mvc;

import neoguri.springTemplate.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    @Query(value = "SELECT m FROM Member m WHERE m.nickname =:nickname")
    Optional<Member> findByNickname(String nickname);

}
