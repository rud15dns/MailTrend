package com.example.mailtrend.MailSend.entity;

@Getter
@Entity
@NoArgsConstructor
public class ImageFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // id

    @Column(nullable = false)           // image 경로
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private User user;                  // userid

    @OneToOne
    @JoinColumn(name = "boardid", nullable = false)
    private Board board;

    public ImageFile(String path, User user, Board board){
        this.path = path;
        this.user = user;
        this.board = board;
    }
}
