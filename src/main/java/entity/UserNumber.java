package entity;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class UserNumber {
	private String phoneNumber ;
	private String principal ;
	private Date subscriptionEndDate;
	private long calls;
}
