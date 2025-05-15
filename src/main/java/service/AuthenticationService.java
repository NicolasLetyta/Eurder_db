package service;

import domain.Member;
import domain.MemberRole;
import exception.InvalidHeaderException;
import org.springframework.stereotype.Service;
import repository.MemberRepository;

import java.util.Base64;

import static utility.Validation.validateArgument;

@Service
public class AuthenticationService {

    private final MemberRepository memberRepository;

    public AuthenticationService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public String encode (String email, String password) {
        String valueToEncode = email + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    public String[] decode(String authHeader) {
        validateArgument(authHeader,"Missing authorization header",String::isEmpty, InvalidHeaderException::new);
        validateArgument(authHeader,"Invalid authorization header", a->!a.startsWith("Basic "), InvalidHeaderException::new);

        String base64 = authHeader.substring(6);
        String decoded = new String(Base64.getDecoder().decode(base64));

        String[] decodedArray = decoded.split(":", 2);
        validateArgument(decodedArray,"Invalid Authorization format", array->array.length!=2, InvalidHeaderException::new);

        return decodedArray;
    }

    public Member authenticateMember(String authHeader) {
        String[] decodedArray = decode(authHeader);
        String email = decodedArray[0];
        String password = decodedArray[1];

        validateArgument(email,"Provided email not found in member repo",
                e->!memberRepository.existsByEmail(e), InvalidHeaderException::new);

        Member member = memberRepository.findByEmail(email);

        validateArgument(member,"Invalid password", m->!m.getPassword().equals(password),
                InvalidHeaderException::new);

        return member;
    }

    public Member authenticateAdmin(String authHeader) {
        Member member = authenticateMember(authHeader);
        validateArgument(member,"Member does not have admin privileges",m->!m.getMemberRole().equals(MemberRole.ADMIN),
                InvalidHeaderException::new);

        return member;
        //this comment is here so we can commit this file, ignore
    }
}

